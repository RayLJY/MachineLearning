package Ray.com.segment

import scala.collection.JavaConverters._

/**
  * Created by ray on 17/2/7.
  *
  * This object make using Segment Class more convenient in scala environment.
  *
  */
object SegmentS {

  val segment = new Segment()

  def splitSentence2Words(sentence: String): List[String] = {
    segment.splitSentence2WordList(sentence).asScala.toList
  }

  def splitSentence2Words(sentence: String, regs: Array[String]): List[String] = {
    segment.splitSentence2WordList(sentence, regs).asScala.toList
  }

  def splitCSentence2Words(sentence: String): List[String] = {
    segment.splitCSentence2WordList(sentence).asScala.toList
  }

  def splitESentence2Words(sentence: String): List[String] = {
    segment.splitESentence2WordList(sentence).asScala.toList
  }
}
