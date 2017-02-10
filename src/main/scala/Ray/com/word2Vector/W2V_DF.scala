package Ray.com.word2Vector

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.ml.feature.{Word2Vec, Word2VecModel}
import org.apache.spark.sql.{DataFrame, SQLContext}

/**
  * Created by ray on 17/2/9.
  *
  * Use word2Vec Class to turn word to vector in this object.
  * Format of input data is DataFrame
  */
object W2V_DF {

  /**
    * build word2Vec model
    *
    * @param df every Row includes some words of a sentence/article
    */
  def buildModel(df: DataFrame, inputCol: String, outputCol: String = "wv", minCount: Int = 0, vectorSize: Int = 100, seed: Long = 1L): Word2VecModel = {
    val word2Vec = new Word2Vec()
      .setInputCol(inputCol)
      .setOutputCol(outputCol)
      .setVectorSize(vectorSize)
      .setMinCount(minCount)
      .setSeed(seed)
    val model = word2Vec.fit(df)
    model
  }

  /**
    * build word2Vec model ,then return word and vector that represents word
    *
    * @param df every Row includes some words of a sentence/article
    */
  def getWordsVectors(df: DataFrame, inputCol: String, outputCol: String = "wv", minCount: Int = 0, vectorSize: Int = 100, seed: Long = 1L): DataFrame = {
    val model = buildModel(df, inputCol, outputCol, minCount, vectorSize, seed)
    model.getVectors
  }

  /**
    * in this method, every sentence/article will be turn to a vector
    *
    * @param df every Row includes some words of a sentence/article
    */
  def makeWordsVector(df: DataFrame, inputCol: String, outputCol: String = "wv", minCount: Int = 0, vectorSize: Int = 100, seed: Long = 1L): DataFrame = {
    val model = buildModel(df, inputCol, outputCol, minCount, vectorSize, seed)
    model.transform(df)
  }


  //**********************************************  test area  ********************************************************

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAppName("word2vector DataFrame").setMaster("local")
    val sc = new SparkContext(conf)
    val sql = new SQLContext(sc)

    val df = sql.createDataFrame(Seq(
      "Tom is a stupid cat, and one of friends is a mouse.",
      "Jerry is a smart mouse, he always play game with a cat.",
      "Tom have a deep friendship with Jerry.",
      "Yes, you are right.",
      "Tom just is the cat who Jerry plays game with,",
      "Jerry just is the mouse who makes friends with Tom."
    ).map(_.replaceAll("[\\.\\,]", "").split(" ")).map(Tuple1.apply))
      .toDF("word")

    df.foreach(println)
    df.show()


    val model = buildModel(df, "word", vectorSize = 20)
    model.findSynonyms("Tom", 10).foreach(println)

    val vectors = getWordsVectors(df, "word", vectorSize = 20)
    vectors.foreach(println)

    val sentence = makeWordsVector(df, "word", vectorSize = 20)
    sentence.foreach(println)

  }

}
