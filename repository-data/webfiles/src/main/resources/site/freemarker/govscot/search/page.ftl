<#include "../../include/imports.ftl">

<#-- @ftlvariable name="pageable" type="org.onehippo.cms7.essentials.components.paging.Pageable" -->
<#-- @ftlvariable name="parameters" type="java.util.Map" -->
<#-- @ftlvariable name="index" type="scot.gov.www.beans.SimpleContent" -->

<#assign term = "" />
<#if parameters['q']??>
    <#assign term = parameters['q'][0]?j_string />
</#if>

<div class="layout--search-results">

<div class="grid" id="page-content">
    <div class="grid__item medium--nine-twelfths large--seven-twelfths">
        <#if index??>
            <h1 class="article-header">${index.title?html}</h1>

            <#if isPostcode??>
                <div class="body-content  leader--first-para">
                    Searching for COVID protection levels in areas of Scotland?
                    </br>
                    <a href="/covid-restrictions-lookup">Use the COVID postcode checker.</a>
                </div>
            <#else>
                <div class="body-content  leader--first-para">
                    <@hst.html hippohtml=index.content/>
                </div>
            </#if>

            <div class="search-box search-box--large ">
                <form id="filters" class="search-box__form" method="GET" action="<@hst.link path='/search/'/>">
                    <input type="hidden" id="imagePath" value="<@hst.webfile path='assets/images/icons/' />" />
                    <label class="search-box__label" for="filters-search-term">Search term</label>
                    <div class="filters-input__wrapper">
                        <input value="${term}" name="term" required="" id="filters-search-term" class="search-box__input " type="text" placeholder="Search site">
                        <button type="submit" title="search" class="search-box__button button button--primary">
                            <span class="icon icon--search-white"></span>
                            <span class="hidden">Search</span>
                        </button>
                    </div>
                </form>
            </div>
        </#if>
    </div>
</div>

<div class="grid">
    <div class="grid__item medium--nine-twelfths large--seven-twelfths">
        <@hst.include ref="results"/>
    </div>
</div>

</div>

<@hst.headContribution category="footerScripts">
    <script type="module" src="<@hst.webfile path="/assets/scripts/filtered-list-page.js"/>"></script>
</@hst.headContribution>
<@hst.headContribution category="footerScripts">
    <script nomodule="true" src="<@hst.webfile path="/assets/scripts/filtered-list-page.es5.js"/>"></script>
</@hst.headContribution>

<#if index??>
    <@hst.headContribution category="pageTitle">
        <title>${index.title} - gov.scot</title>
    </@hst.headContribution>
    <@hst.headContribution>
        <meta name="description" content="${index.metaDescription}"/>
    </@hst.headContribution>

    <@hst.link var="canonicalitem" path="/search" canonical=true/>
    <#include "../common/canonical.ftl" />
</#if>
