package Ray.com.machineLearning

import Ray.com.machineLearning.FPGrowth.rating
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.reflect.ClassTag

/**
  * Created by ray on 17/2/16.
  *
  * The Apriori algorithm is not implemented in spark MLlib, neither would I.
  *
  * I creates a tool object that be used to test mining frequent pattern based on the Apriori algorithm principle.
  *
  * In this object, the number of item of frequent pattern can be set by yourself.
  */
object Apriori {

  /**
    * be used to describe data of frequent pattern
    */
  case class FrequentPattern[Item: ClassTag](antecedent: Array[Item],
                                             consequent: Array[Item],
                                             support: Double,
                                             confidence: Double,
                                             lift: Double,
                                             frequentCount: Long)

  /**
    * count the number of every item in all transactions
    */
  def countItem[Item: ClassTag](rdd: RDD[Array[Item]]): RDD[(Array[Item], Long)] = {

    rdd.flatMap(x => x).map((_, 1)).reduceByKey(_ + _)
      .map(pair => (Array(pair._1), pair._2.toLong))
  }

  /**
    * count the number of pattern in all transactions
    */
  def countPattern[Item: ClassTag](rdd: RDD[Array[Item]], pattern: RDD[Array[Item]]): RDD[(Array[Item], Long)] = {

    val r = rdd.collect
    pattern.map { p =>
      val len = p.length
      val count = r.map { transaction =>
        transaction.intersect(p)
      }.count(_.length == len)
      (p, count)
    }
  }

  /**
    * iteratively multiple iterative procedure of make frequent pattern,
    * until the number of item of frequent pattern equal numItem
    */
  def freqPattern[Item: ClassTag](rdd: RDD[Array[Item]], minSupport: Double, numItem: Int = 2) = {

    val size = rdd.count()
    val minCount = Math.ceil(size * minSupport).toLong

    val freqItemCount = countItem(rdd).filter(_._2 >= minCount)

    var freqPatternCount = freqItemCount
    var freqPattern = freqPatternCount.map(_._1)

    for (_ <- 2 to numItem) {
      freqPattern = makeFreqPattern(freqPattern)
      freqPatternCount = countPattern(rdd, freqPattern).filter(_._2 >= minCount)
      freqPattern = freqPatternCount.map(_._1)
    }

    //    freqPatternCount //debug test
    run(freqPatternCount, freqItemCount, size)
  }

  /**
    * create any possible candidate for frequent pattern based on the previous iterator
    */
  def makeFreqPattern[Item: ClassTag](rdd: RDD[Array[Item]]): RDD[Array[Item]] = {

    val arr = rdd.flatMap(i => i).distinct()
    val newRdd = rdd.cartesian(arr).filter(a => !a._1.contains(a._2))
      .map(a => a._1 ++ Array(a._2))
    newRdd
  }

  /**
    * count all attributes of every frequent pattern
    */
  def run[Item: ClassTag](freqPatternCount: RDD[(Array[Item], Long)],
                          freqItemCount: RDD[(Array[Item], Long)],
                          size: Long): RDD[FrequentPattern[Item]] = {

    val freqItem = freqItemCount.collect().toMap

    freqPatternCount.map { patternCount =>
      val pattern = patternCount._1
      val count = patternCount._2.toDouble

      val pat = pattern.splitAt(pattern.length - 1)
      val antecedent = pat._1
      val consequent = pat._2

      val confidence = count / freqItem.filterKeys(_.containsSlice(consequent)).head._2
      val support = count / size
      val lift = confidence / support

      FrequentPattern(antecedent, consequent, support, confidence, lift, count.toLong)
    }
  }

  //**********************************************  test area  ********************************************************

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("Apriori Test").setMaster("local")
    val sc = new SparkContext(conf)

    val minSupport = 0.2
    val numItem = 3
    val minConfidence = 0.8

    //    val path = "data/sample_fpgrowth.txt"
    //    val data = sc.textFile(path).map(_.trim.split(" "))

    val path = "data/ml-100k/u.data"
    val ratingRdd = sc.textFile(path).map(_.split("\t")).map { l => rating(l(0), l(1), l(2), l(3)) }
    val data = ratingRdd.map { r => (r.userId, r.itemId) }.groupByKey.map(_._2.toArray)

    //    countItem(data).foreach(a =>println(a._1.mkString("[",",","]") + "  "+a._2))

    val frequentPattern = freqPattern(data, minSupport, numItem)

    //    frequentPattern.foreach { a =>
    //      println(
    //        a.antecedent.mkString("[", ",", "]") + " => " +
    //          a.consequent.mkString("[", ",", "]") + " :\t" +
    //          " support :" + a.support + "\t" +
    //          " confidence :" + a.confidence + "\t" +
    //          " lift :" + a.lift + "\t" +
    //          " frequentCount :" + a.frequentCount
    //      )
    //    }
    //    debug test
    //    frequentPattern.foreach { a =>
    //      val pattern = a._1
    //      val pat = pattern.splitAt(pattern.length - 1)
    //
    //      val antecedent = pat._1
    //      val consequent = pat._2
    //      println(antecedent.mkString("[", ",", "]") + "   " + consequent.mkString("[", ",", "]"))
    //    }

    //    frequentPattern.map(format(_)).saveAsTextFile("data/res/apriori/1")
    frequentPattern.filter(_.confidence >= minConfidence).map{ fp =>
      fp.antecedent.mkString("[", ",", "]") + " => " +
        fp.consequent.mkString("[", ",", "]") + "\t:\t" +
        "support :" + fp.support + "\t" +
        "confidence :" + fp.confidence + "\t" +
        "lift :" + fp.lift + "\t" +
        "frequentCount :" + fp.frequentCount
    }.saveAsTextFile("data/res/apriori/3")
  }

}
