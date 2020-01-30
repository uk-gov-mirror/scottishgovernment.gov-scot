package scot.gov.www.beans;

import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.content.beans.standard.HippoHtml;
import org.onehippo.cms7.essentials.dashboard.annotations.HippoEssentialsGenerated;
import org.onehippo.forge.feed.api.FeedType;
import org.onehippo.forge.feed.api.annot.SyndicationElement;
import org.onehippo.forge.feed.api.annot.SyndicationRefs;
import org.onehippo.forge.feed.api.transform.CalendarToDateConverter;

import java.util.Calendar;
import java.util.List;

@HippoEssentialsGenerated(internalName = "govscot:News")
@Node(jcrType = "govscot:News")
public class News extends SimpleContent {
    @HippoEssentialsGenerated(internalName = "govscot:title")
    @SyndicationRefs({@SyndicationElement(type = FeedType.RSS, name = "title")})
    public String getTitle() {
        return getProperty("govscot:title");
    }

    @HippoEssentialsGenerated(internalName = "govscot:summary")
    @SyndicationRefs({@SyndicationElement(type = FeedType.RSS, name = "description")})
    public String getSummary() {
        return getProperty("govscot:summary");
    }

    @HippoEssentialsGenerated(internalName = "govscot:seoTitle")
    public String getSeoTitle() {
        return getProperty("govscot:seoTitle");
    }

    @HippoEssentialsGenerated(internalName = "govscot:metaDescription")
    public String getMetaDescription() {
        return getProperty("govscot:metaDescription");
    }

    @HippoEssentialsGenerated(internalName = "hippostd:tags")
    public String[] getTags() {
        return getProperty("hippostd:tags");
    }

    @HippoEssentialsGenerated(internalName = "govscot:background")
    public HippoHtml getBackground() {
        return getHippoHtml("govscot:background");
    }

    @HippoEssentialsGenerated(internalName = "govscot:heroImage")
    public ExternalLink getHeroImage() {
        return getBean("govscot:heroImage", ExternalLink.class);
    }

    @HippoEssentialsGenerated(internalName = "govscot:publicationDate")
    @SyndicationElement(type = FeedType.RSS, name = "pubDate", converter = CalendarToDateConverter.class)
    public Calendar getPublicationDate() {
        return getProperty("govscot:publicationDate");
    }

    @HippoEssentialsGenerated(internalName = "govscot:externalId")
//    @SyndicationElement(type = FeedType.RSS, name = "guid")
    @SyndicationRefs({@SyndicationElement(type = FeedType.RSS, name = "guid")})
    public String getExternalId() {
        return getProperty("govscot:externalId");
    }

    @HippoEssentialsGenerated(internalName = "govscot:updatedDate")
    public Calendar getUpdatedDate() {
        return getProperty("govscot:updatedDate");
    }

    @HippoEssentialsGenerated(internalName = "govscot:attachments")
    public List<ExternalLink> getAttachments() {
        return getChildBeansByName("govscot:attachments", ExternalLink.class);
    }

    @HippoEssentialsGenerated(internalName = "govscot:topics")
    public List<HippoBean> getTopics() {
        return getLinkedBeans("govscot:topics", HippoBean.class);
    }

    @HippoEssentialsGenerated(internalName = "govscot:policyTags")
    public String[] getPolicyTags() {
        return getProperty("govscot:policyTags");
    }

    @HippoEssentialsGenerated(internalName = "govscot:orgRole")
    public List<HippoBean> getOrgRole() {
        return getLinkedBeans("govscot:orgRole", HippoBean.class);
    }

    @HippoEssentialsGenerated(internalName = "govscot:secondaryOrgRole")
    public List<HippoBean> getSecondaryOrgRole() {
        return getLinkedBeans("govscot:secondaryOrgRole", HippoBean.class);
    }

    @HippoEssentialsGenerated(internalName = "govscot:content")
    public HippoHtml getContent() {
        return getHippoHtml("govscot:content");
    }

    @HippoEssentialsGenerated(internalName = "govscot:notes")
    public HippoHtml getNotes() {
        return getHippoHtml("govscot:notes");
    }

    public String getLabel() { return "news"; }

    @SyndicationElement(type = FeedType.RSS, name = "link")
    public String getURL() {
        return "https://www.gov.scot/news/".concat(getProperty("govscot:prglooslug"));
    }
}
