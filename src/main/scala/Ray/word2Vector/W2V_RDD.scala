package Ray.word2Vector

import Ray.com.utils.VectorUtil
import Ray.segment.SegmentS
import Ray.utils.VectorUtil
import org.apache.spark.mllib.feature.{Word2Vec, Word2VecModel}
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by ray on 17/2/9.
  *
  * Use word2Vec Class to turn word to vector in this object.
  * Format of input data is RDD[List[String]]
  *
  */
object W2V_RDD {

  /**
    * build word2Vec model
    *
    * @param rdd every List represents a sentence/article and every word represents a word
    */
  def buildModel(rdd: RDD[List[String]], vectorSize: Int = 100, minCount: Int = 0, seed: Long = 1L): Word2VecModel = {
    val w2v = new Word2Vec()
    w2v.setVectorSize(vectorSize)
    w2v.setMinCount(minCount)
    w2v.setSeed(seed)
    val w2vModel = w2v.fit(rdd)
    w2vModel
  }

  /**
    * build word2Vec model ,then return word and vector that represents word
    *
    * @param rdd every List represents a sentence/article and every word represents a word
    */
  def getWordVectors(rdd: RDD[List[String]], vectorSize: Int = 100, minCount: Int = 0, seed: Long = 1L): Map[String, Array[Float]] = {
    val model = buildModel(rdd, vectorSize, minCount, seed)
    model.getVectors
  }

  /**
    *
    * in this method, every sentence/article will be turn to a vector
    *
    * @param rdd in this rrd, every List represents a sentence/article
    */
  def makeWordsVector(rdd: RDD[List[String]], vectorSize: Int = 100, minCount: Int = 0, seed: Long = 1L): RDD[(List[String], Vector)] = {
    val vectors = getWordVectors(rdd, vectorSize, minCount, seed)
    rdd.map { sentence =>
      val sum = Vectors.zeros(vectorSize)
      if (sentence.nonEmpty) {
        sentence.foreach { word =>
          val v = Vectors.dense(vectors(word).map(_.toDouble))
          VectorUtil.axpy(1.0, v, sum)
        }
        VectorUtil.scal(1.0 / sentence.size, sum)
      }
      (sentence, sum)
    }
  }

  def makeWordsVector(rdd: RDD[List[String]], wordVector: Map[String, Array[Float]], vectorSize: Int): RDD[(List[String], Vector)] = {
    val vectors = wordVector
    rdd.map { sentence =>
      val sum = Vectors.zeros(vectorSize)
      if (sentence.nonEmpty) {
        sentence.foreach { word =>
          val v = Vectors.dense(vectors(word).map(_.toDouble))
          VectorUtil.axpy(1.0, v, sum)
        }
        VectorUtil.scal(1.0 / sentence.size, sum)
      }
      (sentence, sum)
    }
  }

  //**********************************************  test area  ********************************************************

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAppName("Word2Vec RDD").setMaster("local")
    val sc = new SparkContext(conf)
    val path = "data/世界人权宣言.txt"

    val data = sc.textFile(path).filter(_.length > 0)
    data.foreach(println)

    //分词数据集RDD
    val rdd = data.map(SegmentS.splitCSentence2Words)
    rdd.foreach(println)

    //构建 word2Vector 模型
    val model = buildModel(rdd)
    model.findSynonyms("人人", 10).foreach(println)

    //构建 word2Vector 模型，并获取其"词向量"
    val vectors = getWordVectors(rdd)
    vectors.mapValues(_.mkString("[", ",", "]")).foreach(println)

    //构建句子的向量
    val sentence = makeWordsVector(rdd)
    sentence.foreach(println)


    //构建整个文章的向量
    val rdd2 = sc.wholeTextFiles(path).map(p => SegmentS.splitCArticle2Words(p._2))
    val article = makeWordsVector(rdd2)
    article.foreach(println)
  }
}
