package scot.gov.codec;

import org.hippoecm.repository.api.StringCodecFactory;

import java.text.Normalizer;

/**
 * Created by z418868 on 15/05/2019.
 */
public class UriCodecPeriodFix extends StringCodecFactory.UriEncoding {

    @Override
    public String encode(final String input) {
        String encoded = super.encode(input);

        // replace all perdiods with full stops:
        char[] chars = Normalizer.normalize(input, Normalizer.Form.NFC).toCharArray();

        StringBuffer sb = new StringBuffer();
        boolean lastSpace = true;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '.') {
                sb.append("-");
            } else {
                sb.append(chars[i]);
            }
        }
        return  sb.toString();
    }
}
