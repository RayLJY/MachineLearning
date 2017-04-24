package Ray.segment;

import org.wltea.analyzer.cfg.Configuration;
import org.wltea.analyzer.cfg.DefaultConfig;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ray on 17/4/22.
 * <p>
 * Using IKAnalyzer Tool to parse word.
 * I using default method to construct Configuration, which means will loading
 * IKAnalyzer.cfg.xml file and other two files, exit.dic and stopword.dic.
 * Be used in multi-thread environment.
 */
public class IKSegment {
    private Configuration configuration;

    private static String[] ECharacter = {"[^a-zA-Z0-9]"};

    private static String[] CCharacter = {"[^\u4e00-\u9fa5]"};

    public IKSegment(boolean smart) {
        configuration = DefaultConfig.getInstance();
        configuration.setUseSmart(smart);
    }

    public List<String> splitSentence2WordList(String sentence) {
        ArrayList<String> list = new ArrayList<>();

        IKSegmenter ik = new IKSegmenter(new StringReader(sentence), configuration);
        Lexeme lexeme;
        try {
            while ((lexeme = ik.next()) != null) {
                String word = lexeme.getLexemeText();
                list.add(word);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<String> splitSentence2WordList(String sentence, String[] regs) {
        for (String reg : regs) {
            sentence = sentence.replaceAll(reg, " ");
        }
        return splitSentence2WordList(sentence);
    }

    public List<String> splitCSentence2WordList(String sentence) {
        return splitSentence2WordList(sentence, CCharacter);
    }

    public List<String> splitESentence2WordList(String sentence) {
        return splitSentence2WordList(sentence, ECharacter);
    }

    public static void main(String[] args) {
        String str1 = "Lots of periodicals in foreign (languages) have </p>been<p>" +
                " {subscribed} to, [not] to mention those in Chinese.;:\'\"/\\";
        String str2 = "土生土长的深圳卫视【编】导阿豆，带你行走找寻深圳静谧的" +
                "另一面<p>，带你看深圳《本地》人眼中的「大」城深圳。</p><p>海景" +
                "漫步or城市风光，随心选择你享受的惬意深圳。</p><p>你在看风景，我在看" +
                "你，边走边拍风景中的你。</p>";

        IKSegment ik = new IKSegment(true);
        List<String> list = ik.splitSentence2WordList(str1);
        System.out.println(list);

        list = ik.splitESentence2WordList(str1);
        System.out.println(list);

        list = ik.splitSentence2WordList(str2);
        System.out.println(list);

        list = ik.splitCSentence2WordList(str2);
        System.out.println(list);
    }
}
