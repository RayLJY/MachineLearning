package Ray.com.word2Vector

import Ray.com.Utils.VectorUtil
import Ray.com.segment.SegmentS
import org.apache.spark.mllib.feature.Word2Vec
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by ray on 17/2/9.
  */
object W2V {

  def buildModel(rdd:RDD[List[String]],vectorSize:Int = 100,minCount:Int=0)={

    val w2v= new Word2Vec()
    w2v.setVectorSize(vectorSize)
    w2v.setMinCount(minCount)
    w2v.setSeed(1)
    val w2vModel=w2v.fit(rdd)
    w2vModel
  }

  def makeWordVectors(rdd:RDD[List[String]],vectorSize:Int = 100):Map[String, Array[Float]]={
    val model = buildModel(rdd,vectorSize)
    model.getVectors
  }

  def makeSentenceVector(rdd:RDD[List[String]],vectorSize:Int = 100)={
    val vectors=makeWordVectors(rdd,vectorSize)
    rdd.map{ sentence=>
      val sum = Vectors.zeros(vectorSize)
      if (sentence.nonEmpty) {
        sentence.foreach { word =>
          val v = Vectors.dense(vectors(word).map(_.toDouble))
          VectorUtil.axpy(1.0, v, sum)
        }
        VectorUtil.scal(1.0 / sentence.size, sum)
      }
      (sentence,sum)
    }
  }


  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAppName("Word2Vec RDD").setMaster("local")
    val sc = new SparkContext(conf)
//    val path = "data/世界人权宣言.txt"
    val path = "data/a.txt"

    val data = sc.textFile(path).filter(_.length > 0)
//    data.foreach(println)

    //分词数据集RDD
    val rdd = data.map(SegmentS.splitCSentence2Words)
//    rdd.foreach(println)


//    val w2vModel=buildModel(rdd)
//    val w = w2vModel.transform("人人")
//    println(w)

    makeWordVectors(rdd,3).mapValues(_.mkString("[",",","]")).foreach(println)

    makeSentenceVector(rdd,3).mapValues(_.toArray.mkString("[",",","]")).foreach(println)

  }
}
