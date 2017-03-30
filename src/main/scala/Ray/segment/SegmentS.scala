package Ray.segment

import scala.collection.JavaConverters._

/**
  * Created by ray on 17/2/7.
  *
  * This object make class Ray.com.segment.Segment more convenient in scala environment.
  *
  */
object SegmentS {

  val segment = new Segment()

  /**
    * split a sentence to a list of word
    */
  def splitSentence2Words(sentence: String): List[String] =
    segment.splitSentence2WordList(sentence).asScala.toList


  /**
    * replaces every substring in this sentence that matches the given regular expression,
    * then split this sentence to a list of word
    */
  def splitSentence2Words(sentence: String, regs: Array[String]): List[String] =
    segment.splitSentence2WordList(sentence, regs).asScala.toList


  /**
    * replaces every substring in this sentence composed of Chinese characters that matches
    * the default (Ray.com.segment.Segment.cPunctuation is Chinese punctuation Array) regular expression,
    * then split this sentence to a list of word
    */
  def splitCSentence2Words(sentence: String): List[String] =
    segment.splitCSentence2WordList(sentence).asScala.toList


  /**
    * replaces every substring in this sentence composed of English characters that matches
    * the default (Ray.com.segment.Segment.ePunctuation is English punctuation Array) regular expression,
    * then split this sentence to a list of word
    *
    */
  def splitESentence2Words(sentence: String): List[String] =
    segment.splitESentence2WordList(sentence).asScala.toList

  /**
    * split a article to a list of word
    */
  def splitArticle2Words(article: String): List[String] =
    splitSentence2Words(article.replaceAll("\\n", ""))


  /**
    * replaces every substring in this article that matches the given regular expression,
    * then split this sentence to a list of word
    */
  def splitArticle2Words(article: String, regs: Array[String] = Array("")): List[String] =
    splitSentence2Words(article.replaceAll("\\n", ""), regs)


  /**
    * replaces every substring in this article composed of Chinese characters that matches
    * the default (Ray.com.segment.Segment.cPunctuation is Chinese punctuation Array) regular expression,
    * then split this sentence to a list of word
    */
  def splitCArticle2Words(article: String): List[String] =
    splitCSentence2Words(article.replaceAll("\\n", ""))


  /**
    * replaces every substring in this article composed of English characters that matches
    * the default (Ray.com.segment.Segment.ePunctuation is English punctuation Array) regular expression,
    * then split this sentence to a list of word
    */
  def splitEArticle2Words(article: String): List[String] =
    splitESentence2Words(article.replaceAll("\\n", ""))

}
