package Ray.segment;

import org.lionsoul.jcseg.tokenizer.ASegment;
import org.lionsoul.jcseg.tokenizer.core.*;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ray on 17/2/6.
 * <p>
 * This class that split sentences to words
 * by Jcseg jar is a basic class
 */
public class Segment implements Serializable {

    private ASegment seg = null;

    private static String[] ePunctuation = ", \\. : ; ' \" / \\\\ \\[ \\] \\{ \\} \\| \\( \\) \\s - ! < > \\? _ @".split(" ");

    private static String[] cPunctuation = "，,。,《,》,：,；,「,」,【,】,￥,“,”,、,（,）,．".split(",");

    public Segment() {
        JcsegTaskConfig config = new JcsegTaskConfig(Segment.class.getResource("").getPath() + "/jcseg.properties");
//        JcsegTaskConfig config = new JcsegTaskConfig();
        ADictionary dic = DictionaryFactory.createDefaultDictionary(config);
        try {
            seg = (ASegment) SegmentFactory.createJcseg(JcsegTaskConfig.COMPLEX_MODE, config, dic);
        } catch (JcsegException e) {
            e.printStackTrace();
        }
    }

    /**
     * split a sentence to a list of word
     */
    public List<String> splitSentence2WordList(String sentence) {
        ArrayList<String> list = new ArrayList<>();
        try {
            seg.reset(new StringReader(sentence));
            IWord word;
            while ((word = seg.next()) != null) {
                list.add(word.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }


    /**
     * replaces every substring in this sentence that matches the given regular expression,
     * then split this sentence to a list of word
     */
    public List<String> splitSentence2WordList(String sentence, String[] regs) {
        for (String reg : regs) {
            sentence = sentence.replaceAll(reg, " ");
        }
        return splitSentence2WordList(sentence);
    }

    /**
     * replaces every substring in this sentence composed of Chinese characters that matches
     * the default (cPunctuation is Chinese punctuation Array) regular expression,
     * then split this sentence to a list of word
     *
     * @param sentence Chinese sentence
     */
    public List<String> splitCSentence2WordList(String sentence) {
        return splitSentence2WordList(sentence, cPunctuation);
    }

    /**
     * replaces every substring in this sentence composed of English characters that matches
     * the default (ePunctuation is English punctuation Array) regular expression,
     * then split this sentence to a list of word
     *
     * @param sentence English sentence
     */
    public List<String> splitESentence2WordList(String sentence) {
        return splitSentence2WordList(sentence, ePunctuation);
    }

    //***********************************************  test area  ****************************************************

    public static void main(String[] args) {
        String str1 = "Lots of periodicals in foreign (languages) have </p>been<p>" +
                " {subscribed} to, [not] to mention those in Chinese.;:\'\"/\\";

        String str2 = "土生土长的深圳卫视【编】导阿豆，带你行走找寻深圳静谧的" +
                "另一面<p>，带你看深圳《本地》人眼中的「大」城深圳。</p><p>海景" +
                "漫步or城市风光，随心选择你享受的惬意深圳。</p><p>你在看风景，我在看" +
                "你，边走边拍风景中的你。</p>";

        Segment s = new Segment();
        List<String> sl = s.splitSentence2WordList(str2);
        System.out.println(sl);
        sl = s.splitCSentence2WordList(str2);
        System.out.println(sl);
        sl = s.splitESentence2WordList(str1);
        System.out.println(sl);
    }
}