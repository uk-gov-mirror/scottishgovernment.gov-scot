package scot.gov.www.beans;

import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoHtml;

@Node(jcrType = "govscot:articledocument")
public class ArticleDocument extends SimpleDocument {

    public HippoHtml getAdditional() {
        return getHippoHtml("govscot:additional");
    }
}
