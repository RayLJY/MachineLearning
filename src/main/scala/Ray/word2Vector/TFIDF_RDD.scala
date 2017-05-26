package Ray.word2Vector

import Ray.segment.SegmentS
import org.apache.spark.mllib.feature.{HashingTF, IDF}
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

/**
  * Created by ray on 17/2/7.
  *
  * TF-IDF algorithm is used to turn word to vector in this object.
  * Format of input data is RDD[List[String]]
  *
  */
object TFIDF_RDD {

  /**
    * turns a word to a hash value and calculates it's TF value
    */
  def makeVectorTF(rdd: RDD[List[String]], numFeatures: Int): RDD[Vector] = {
    val hashingTF = new HashingTF(numFeatures)
    hashingTF.transform(rdd)
  }

  /**
    * turns a word to a hash value and calculates it's TF-IDF value
    */
  def makeVectorTFIDF(rdd: RDD[List[String]], numFeatures: Int): RDD[Vector] = {
    val idf = new IDF()
    val tf = makeVectorTF(rdd, numFeatures)
    val idfModel = idf.fit(tf)
    idfModel.transform(tf)
  }

  /**
    * calculates TF-IDF value of words
    */
  def makeVectorTFIDF(rv: RDD[Vector]): RDD[Vector] = {
    val idf = new IDF()
    val idfModel = idf.fit(rv)
    idfModel.transform(rv)
  }

  /**
    * turns a word to a hash value and calculates it's TF value,
    * then changes into labeledPoint format
    */
  def makeLabeledPointTF(rdd: RDD[List[String]], numFeatures: Int, category: Double): RDD[LabeledPoint] = {
    val tf = makeVectorTF(rdd, numFeatures)
    Vector2LabeledPoint(tf, category)
  }

  /**
    * turns a word to a hash value and calculates it's TF-IDF value,
    * then changes into labeledPoint format
    */
  def makeLabeledPointTFIDF(rdd: RDD[List[String]], numFeatures: Int, category: Double): RDD[LabeledPoint] = {
    val tfIdf = makeVectorTFIDF(rdd, numFeatures)
    Vector2LabeledPoint(tfIdf, category)
  }

  /**
    * changes vector format to labeledPoint format and marks category
    */
  def Vector2LabeledPoint(rv: RDD[Vector], category: Double): RDD[LabeledPoint] = {
    rv.map { v =>
      LabeledPoint(category, v.toSparse)
    }
  }

  //**********************************************  test area  ********************************************************

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
    .appName("TF-IDF RDD")
    .master("local")
    .getOrCreate()

    val sc = spark.sparkContext
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

    // close spark session
    spark.close()
  }
}