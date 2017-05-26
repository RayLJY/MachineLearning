package Ray.word2Vector

import Ray.segment.SegmentS
import org.apache.spark.ml.feature.{HashingTF, IDF}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.ml.linalg.SparseVector
import org.apache.spark.mllib.linalg.Vectors

/**
  * Created by ray on 17/2/8.
  *
  * TF-IDF algorithm is used to turn word to vector in this object.
  * Format of input data is DataFrame
  */
object TFIDF_DF {

  /**
    * calculates TF value and adds value into dataFrame
    */
  def dataFrameAddTF(dataFrame: DataFrame, inputCol: String, outputCol: String, numFeatures: Int): DataFrame = {
    val hashingTF = new HashingTF().setNumFeatures(numFeatures).setInputCol(inputCol).setOutputCol(outputCol)
    hashingTF.transform(dataFrame)
  }

  /**
    * calculates TF-IDF value and adds the value into dataFrame
    */
  def dataFrameAddTFIDF(dataFrame: DataFrame, inputCol: String, outputCol: String): DataFrame = {
    val idf = new IDF()
    idf.setInputCol(inputCol)
    idf.setOutputCol(outputCol)
    val idfModel = idf.fit(dataFrame)
    //新增TF-IDF列到DataFrame中,输出格式(特征总数,[词1hash值,...,词N hash值],[词1 tfIdf值,...,词N tfIdf值])
    idfModel.transform(dataFrame)
  }

  /**
    * calculates TF value and adds value into dataFrame
    * then changes into labeledPoint format
    */
  def makeLabeledPointTF(dataFrame: DataFrame, inputCol: String, outputCol: String, numFeatures: Int, category: Double): RDD[LabeledPoint] = {
    val tf = dataFrameAddTF(dataFrame, inputCol, outputCol, numFeatures)
    dataFrame2LabeledPoint(tf, outputCol, category)
  }

  /**
    * calculates TF-IDF value and adds value into dataFrame
    * then changes into labeledPoint format
    */
  def makeLabeledPointTFIDF(dataFrame: DataFrame, inputCol: String, outputCol: String, category: Double): RDD[LabeledPoint] = {
    val tfIdf = dataFrameAddTFIDF(dataFrame, inputCol, outputCol)
    dataFrame2LabeledPoint(tfIdf, outputCol, category)
  }

  /**
    * changes dataFrame format to LabeledPoint format and marks category
    */
  def dataFrame2LabeledPoint(dataFrame: DataFrame, tfIdf: String, category: Double): RDD[LabeledPoint] = {
    dataFrame.select(tfIdf).rdd
      .map{row =>
        val v = row.getAs[SparseVector](0)
        // Vectors.fromML(v) // org.apache.spark.ml.linalg.SparseVector 转 org.apache.spark.mllib.linalg.Vector
        LabeledPoint(category,Vectors.fromML(v))
      }
  }

  //**********************************************  test area  ********************************************************

  case class dataSet(id: Int, words: List[String])

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("TF-IDF DataFrame")
      .master("local")
      .getOrCreate()

    val sc = spark.sparkContext
    import spark.implicits._

    val path = "data/p1"

    val data = sc.wholeTextFiles(path)
    data.foreach(println)

    //分词数据集DataFrame
    val df = data.map { m =>
      val id = m._1.split("/").last.toInt
      val words = SegmentS.splitESentence2Words(m._2)
      dataSet(id, words)
    }.toDF
    df.show()

    //计算 TF 值
    val tfDF = dataFrameAddTF(df, "words", "tf", 10000)
    tfDF.foreach(println(_))
    tfDF.select("tf").foreach(println(_))

    //计算 TF-IDF 值
    val tfIdfDF = dataFrameAddTFIDF(tfDF, "tf", "tfIdf")
    tfIdfDF.foreach(println(_))
    tfIdfDF.select("tf", "tfIdf").foreach(println(_))

    //计算 TF 值 LabeledPoint 格式
    val tfLP = makeLabeledPointTF(df, "words", "tf", 10000, 1.0)
    tfLP.foreach(println)

    //计算 TF-IDF 值 LabeledPoint 格式
    val tfIdfLP = makeLabeledPointTFIDF(tfDF, "tf", "tfIdf", 1.0)
    tfIdfLP.foreach(println)

    // close spark session
    spark.close()
  }
}
