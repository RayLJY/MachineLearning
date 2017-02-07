package Ray.com.segment

import java.util

/**
  * Created by ray on 17/2/7.
  */
object SegmentS {

  val segment = new Segment()

  def splitSentence2Words(sentence: String): util.List[String]={
    segment.splitSentence2WordList(sentence)
  }

  def splitSentence2Words(sentence: String,regs:Array[String]): util.List[String]={
    segment.splitSentence2WordList(sentence,regs)
  }

  def splitCSentence2Words(sentence: String): util.List[String]={
    segment.splitCSentence2WordList(sentence)
  }

  def splitESentence2Words(sentence: String): util.List[String]={
    segment.splitESentence2WordList(sentence)
  }
}
