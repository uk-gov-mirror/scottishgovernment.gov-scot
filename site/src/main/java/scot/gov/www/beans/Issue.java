package scot.gov.www.beans;

import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.content.beans.standard.HippoHtml;
import org.onehippo.cms7.essentials.dashboard.annotations.HippoEssentialsGenerated;

import java.util.Calendar;
import java.util.List;

@HippoEssentialsGenerated(internalName = "govscot:Issue")
@Node(jcrType = "govscot:Issue")
public class Issue extends SimpleContent {
    @HippoEssentialsGenerated(internalName = "govscot:featureDate")
    public Calendar getFeatureDate() {
        return getProperty("govscot:featureDate");
    }

    @HippoEssentialsGenerated(internalName = "govscot:featureDateTitle")
    public String getFeatureDateTitle() {
        return getProperty("govscot:featureDateTitle");
    }

    @HippoEssentialsGenerated(internalName = "govscot:featuredItemsTitle")
    public String getFeaturedItemsTitle() {
        return getProperty("govscot:featuredItemsTitle");
    }

    @HippoEssentialsGenerated(internalName = "govscot:includeFeedback")
    public Boolean getIncludeFeedback() {
        return getProperty("govscot:includeFeedback");
    }

    @HippoEssentialsGenerated(internalName = "govscot:showOnTopicsLandingPage")
    public Boolean getShowOnTopicsLandingPage() {
        return getProperty("govscot:showOnTopicsLandingPage");
    }

    @HippoEssentialsGenerated(internalName = "govscot:featureDateSummary")
    public String getFeatureDateSummary() {
        return getProperty("govscot:featureDateSummary");
    }

    @HippoEssentialsGenerated(internalName = "govscot:overview")
    public HippoHtml getOverview() {
        return getHippoHtml("govscot:overview");
    }

    @HippoEssentialsGenerated(internalName = "govscot:featuredItems")
    public List<HippoBean> getFeaturedItems() {
        return getLinkedBeans("govscot:featuredItems", HippoBean.class);
    }

    @HippoEssentialsGenerated(internalName = "govscot:image")
    public BannerImages getImage() {
        return getLinkedBean("govscot:image", BannerImages.class);
    }

    @HippoEssentialsGenerated(internalName = "govscot:newsTags")
    public String[] getNewsTags() { return getProperty("govscot:newsTags"); }
}
