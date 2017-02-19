package Ray.com.machineLearning

import java.{util => ju}

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext, SparkException}

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

  val Separator = ","

  /**
    * be used to describe data of frequent pattern
    */
  case class FrequentPattern[Item: ClassTag](antecedent: Array[Item],
                                             consequent: Array[Item],
                                             support: Double,
                                             confidence: Double,
                                             lift: Double)

  /**
    * count the number of every item in all transactions
    */
  def countItemFreq[Item: ClassTag](rdd: RDD[Array[Item]], minCount: Long): Array[Item] = {

    rdd.flatMap(x => x.toSeq)
      .map((_, 1L))
      .reduceByKey(_ + _)
      .filter(_._2 >= minCount)
      .collect
      .sortBy(-_._2)
      .map(_._1)
  }

  /**
    * use the rank of frequent item represent item
    */
  def Item2Rank[Item: ClassTag](rdd: RDD[Array[Item]], itemRanks: Map[Item, Int]): RDD[Array[Int]] = {
    rdd.map { transaction =>
      val filtered = transaction.map(item => itemRanks.getOrElse(item, -1)).filter(_ >= 0)
      ju.Arrays.sort(filtered)
      filtered
    }
  }

  /**
    * iteratively multiple iterative procedure of make frequent pattern,
    * until the number of item of frequent pattern equal numItem
    */
  def freqPattern[Item: ClassTag](rdd: RDD[Array[Item]], minSupport: Double, numItem: Int = 2) = {

    if (numItem < 2) {
      throw new IllegalArgumentException(" the number of item of frequent patter must be greater than 1")
    }

    val size = rdd.count()
    val minCount = Math.ceil(size * minSupport).toLong

    val freqItem = countItemFreq(rdd, minCount)

    val itemRanks = freqItem.zipWithIndex.toMap

    var freqRank = itemRanks.values.toArray

    val item2Rank = Item2Rank(rdd, itemRanks)

    var antecedentCount: RDD[(String, Long)] = null
    var freqPatternCount: RDD[(String, Long)] = null

    for (i <- 1 to numItem) {

      freqPatternCount = item2Rank.filter(_.length >= i)
        .flatMap { transaction =>
          val filtered = transaction.intersect(freqRank)
          genFreqItemSets(filtered, i)
        }.map(a => (a.mkString(Separator), 1L))
        .reduceByKey(_ + _)
        .filter(_._2 >= minCount)

      if(freqPatternCount.isEmpty()){
        throw new SparkException(
          s"we don't fund the frequent pattern that the number of item is $i," +
          s"please try to reset parameter of numItem.")
      }

      if (i < numItem - 1) {
        antecedentCount = freqPatternCount
      }

      freqRank = freqPatternCount.flatMap { a =>
        a._1.split(Separator)
          .map(_.toInt)
      }
        .distinct()
        .collect

    }
    //    freqPatternCount.union(antecedentCount) //debug test code
    run(freqPatternCount, antecedentCount, size, freqItem)
  }

  /**
    * create any possible candidate for frequent pattern based on a transaction
    */
  def genFreqItemSets[Item: ClassTag](transaction: Array[Int], numItem: Int): List[Array[Int]] = {
    transaction.combinations(numItem).toList
  }

  /**
    * count all attributes of every frequent pattern
    */
  def run[Item: ClassTag](freqPatternCount: RDD[(String, Long)],
                          antecedentCount: RDD[(String, Long)],
                          size: Long,
                          freqItem: Array[Item]): RDD[FrequentPattern[Item]] = {

    val freqItemSetCount = antecedentCount.map(a => (a._1.split(Separator), a._2))
      .collect()
      .toMap

    freqPatternCount.flatMap { patternCount =>
      val pattern = patternCount._1.split(Separator)
      val patternFreq = patternCount._2

      val antecedents = freqItemSetCount.filterKeys(pattern.diff(_).length == 1)

      antecedents.map { antecedentC =>
        val antecedent = antecedentC._1
        val antecedentFreq = antecedentC._2
        val consequent = pattern.diff(antecedent)
        val confidence = 1.0 * patternFreq / antecedentFreq
        val support = 1.0 * patternFreq / size
        val lift = confidence / support
        FrequentPattern(antecedent.map(a => freqItem(a.toInt)),
          consequent.map(a => freqItem(a.toInt)),
          support, confidence, lift)
      }
    }
  }

  //**********************************************  test area  ********************************************************

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("Apriori Test").setMaster("local")
    val sc = new SparkContext(conf)

    val minSupport = 0.2
    val numItem = 5
    val minConfidence = 0.0

    val path = "data/sample_fpgrowth.txt"
    val data = sc.textFile(path).map(_.trim.split(" "))

    //    val path = "data/ml-100k/u.data"
    //    val ratingRdd = sc.textFile(path).map(_.split("\t")).map { l => rating(l(0), l(1), l(2), l(3)) }
    //    val data = ratingRdd.map { r => (r.userId, r.itemId) }.groupByKey.map(_._2.toArray)

    //    countItemFreq(data, 2l).foreach(a => println(a))

    val frequentPattern = freqPattern(data, minSupport, numItem)
    //debug test code
    //    frequentPattern.foreach { a =>
    //      println(
    //        a._1.mkString("[", ",", "]") + "   " + a._2
    //      )
    //    }

    frequentPattern.foreach { a =>
      println(
        a.antecedent.mkString("[", ",", "]") + " => " +
          a.consequent.mkString("[", ",", "]") + " :\t" +
          " support :" + a.support + "\t" +
          " confidence :" + a.confidence + "\t" +
          " lift :" + a.lift
      )
    }

    frequentPattern.filter(_.confidence >= minConfidence).map { fp =>
      fp.antecedent.mkString("[", ",", "]") + " => " +
        fp.consequent.mkString("[", ",", "]") + "\t:\t" +
        "support :" + fp.support + "\t" +
        "confidence :" + fp.confidence + "\t" +
        "lift :" + fp.lift
    }
//      .foreach(println)
  }

}
