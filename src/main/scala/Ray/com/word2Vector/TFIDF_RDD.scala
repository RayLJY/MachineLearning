package Ray.com.word2Vector

import Ray.com.segment.SegmentS
import org.apache.spark.mllib.feature.{HashingTF, IDF}
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by ray on 17/2/7.
  *
  * TF-IDF algorithm is used to turn word to vector in this object.
  * Format of input data is RDD[List[String]]
  *
  */
object TFIDF_RDD {

  def makeVectorTF(rdd: RDD[List[String]], numFeatures: Int): RDD[Vector] = {
    val hashingTF = new HashingTF(numFeatures)
    hashingTF.transform(rdd)
  }

  def makeVectorTFIDF(rdd: RDD[List[String]], numFeatures: Int): RDD[Vector] = {
    val idf = new IDF()
    val tf = makeVectorTF(rdd, numFeatures)
    val idfModel = idf.fit(tf)
    idfModel.transform(tf)
  }

  def makeVectorTFIDF(rv: RDD[Vector]): RDD[Vector] = {
    val idf = new IDF()
    val idfModel = idf.fit(rv)
    idfModel.transform(rv)
  }

  def makeLabeledPointTF(rdd: RDD[List[String]], numFeatures: Int, category: Double): RDD[LabeledPoint] = {
    val tf = makeVectorTF(rdd, numFeatures)
    Vector2LabeledPoint(tf, category)
  }

  def makeLabeledPointTFIDF(rdd: RDD[List[String]], numFeatures: Int, category: Double): RDD[LabeledPoint] = {
    val tfIdf = makeVectorTFIDF(rdd, numFeatures)
    Vector2LabeledPoint(tfIdf, category)
  }

  def Vector2LabeledPoint(rv: RDD[Vector], category: Double): RDD[LabeledPoint] = {
    rv.map { v =>
      LabeledPoint(category, v.toSparse)
    }
  }

  //**********************************************  测试区  ***********************************************************

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAppName("TF-IDF RDD").setMaster("local")
    val sc = new SparkContext(conf)
    val path = "data/世界人权宣言.txt"

    val data = sc.textFile(path).filter(_.length > 0)
    data.foreach(println)

    //分词数据集RDD
    val rdd = data.map(SegmentS.splitCSentence2Words)

    //将分词数据集转换成 Vector -- 计算 TF 值
    val tf = TFIDF_RDD.makeVectorTF(rdd, 10000)
    tf.foreach(println)

    //将分词数据集转换成 Vector -- 计算 TF-IDF 值
    val tfIdf = TFIDF_RDD.makeVectorTFIDF(rdd, 10000)
    tfIdf.foreach(println)

    val tfIdf2 = TFIDF_RDD.makeVectorTFIDF(tf)
    tfIdf2.foreach(println)

    //将分词数据集转换成 LabeledPoint -- 计算 TF 值
    val tfLP = TFIDF_RDD.makeLabeledPointTF(rdd, 1000, 1.0)
    tfLP.foreach(println)

    //将分词数据集转换成 LabeledPoint -- 计算 TF-IDF 值
    val tfIdfLP = TFIDF_RDD.makeLabeledPointTFIDF(rdd, 1000, 1.0)
    tfIdfLP.foreach(println)
  }
}