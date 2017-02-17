package Ray.com.machineLearning

import org.apache.spark.mllib.fpm.FPGrowth
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by ray on 17/2/16.
  *
  * The FP-growth algorithm is used to mine frequent pattern of movies in this object.
  *
  * we assume all movies that one people rating as a transaction,
  * finally, all data will form a data set of transactions.
  *
  */
object FPGrowth {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("Test FPGrowth").setMaster("local")
    val sc = new SparkContext(conf)
    val path = "data/ml-100k/u.data"
    val ratingRdd = sc.textFile(path).map(_.split("\t")).map { l => rating(l(0), l(1), l(2), l(3)) }

    val rdd = ratingRdd.map { r => (r.userId, r.itemId) }.groupByKey.map(_._2.toArray)

    val minSupport = 0.2
    val minConfidence = 0.8

    val fpg = new FPGrowth()
      .setMinSupport(minSupport)
      .setNumPartitions(1)
    val model = fpg.run(rdd)

    //print frequent item and count
    //    model.freqItemsets.collect().foreach { itemSet =>
    //      println(itemSet.items.mkString("[", ",", "]") + ", " + itemSet.freq)
    //    }
    //
    //    model.generateAssociationRules(minConfidence).collect().foreach { rule =>
    //      println(
    //        rule.antecedent.mkString("[", ",", "]")
    //          + " => " + rule.consequent.mkString("[", ",", "]")
    //          + ", " + rule.confidence)
    //    }

    //  text Apriori object
    model.generateAssociationRules(minConfidence).map { rule =>
      rule.antecedent.mkString("[", ",", "]") + " => " + rule.consequent.mkString("[", ",", "]") + ": " + rule.confidence
    }.saveAsTextFile("data/res/apriori/2")

  }

  case class rating(userId: String, itemId: String, rating: String, timestamp: String)

}
