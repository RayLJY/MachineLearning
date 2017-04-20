package Ray.ray

import Ray.segment.SegmentS
import Ray.word2Vector.W2V_RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by Ray on 17/4/17.
  */
object Trip {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("Trip").setMaster("local[4]")
    val sc = new SparkContext(conf)
    val path = "/Users/ray/tmp/trip/srcD/tripAll.txt"
//    val path = "/Users/ray/tmp/trip/srcD/par.txt"

    val rdd = sc.textFile(path).map { line =>
      val record = line.split("\t")
      if (record.length<3) {
        trip("-1", "", "")
      } else {
        trip(record(0), record(1), record(2))
      }
    }

    val records = rdd.groupBy { r => (r.id, r.name) }.map { t =>
      val texts = t._2.map(_.text)
      (t._1, texts.mkString(" "))
    }

    val record_to_Words = records.map { p => (p._1, SegmentS.splitCArticle2Words(p._2)) }

    val wc =record_to_Words
      .flatMap(_._2)
      .map((_, 1))
      .reduceByKey(_ + _)

    val totalC=wc.map(_._2).fold(0)(_+_)
    println("totalC  " + totalC)
    println("count  "+wc.count())


//    val pre=totalC * 0.0002
    val pre=3000
    println("pre num  " + pre)

    val wc1=wc.filter(c => {c._2 > 3 && c._2 < pre })

    val totalC1=wc1.map(_._2).fold(0)(_+_)

    println("totalC1  " + totalC1)
    println("count1   "+wc1.count())

//      wc1.sortBy(_._2, false)
//      .map(a => s"${a._1}\t${a._2}")
//      .saveAsTextFile("res/1")

    val list=wc1.map(_._1).collect()

    val tem = record_to_Words.map(a=>(a._1,a._2.filterNot(list.contains(_)))).sample(false,0.01,10l)

    val W2V = W2V_RDD.getWordVectors(tem.map(_._2))

    println(W2V.size)

    val record_to_Vector = tem.map{Plist => (Plist._1,W2V_RDD.makeWordsVectors(Plist._2,W2V))}
    record_to_Vector.map{line =>
      s"${line._1} : ${line._2._1.mkString("[",",","]")}   ${line._2._2.toArray.mkString("[",",","]")}"
    }.saveAsTextFile("res/heheda")
  }

  case class trip(id: String, name: String, text: String)

}
