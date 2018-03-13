<#include "../include/imports.ftl">

<nav class="page-group">
    <!-- toggler -->
    <button type="button" class="page-group__toggle visible-xsmall js-show-page-group-list">Choose section &hellip;</button>
    <!-- nav links -->
    <ul class="page-group__list">
        <li class="page-group__item page-group__item--level-0">
            <#if document == policy>
                <span class="page-group__link page-group__link--level-0 page-group__link--selected page-group__link--level-0--selected">
                    <span class="page-group__text">Overview</span>
                </span>
            <#else>
                <@hst.link var="link" hippobean=policy/>
                <a class="page-group__link page-group__link--level-0" href="${link}">
                    <span class="page-group__text">Overview</span>
                </a>
            </#if>
        </li>

        <#-- todo: Latest -->
        <li class="page-group__item page-group__item--level-0">
          <span class="page-group__link page-group__link--level-0">
              <span class="page-group__text">Latest</span>
          </span>
        </li>

        <li class="page-group__item page-group__item--level-0">
          <span class="page-group__link page-group__link--level-0">
              <span class="page-group__text">Policy actions</span>
          </span>
        </li>

        <#list policyDetails as policyDetail>
            <li class="page-group__item page-group__item--level-1">
                <#if document.title == policyDetail.title>
                    <span class="page-group__link page-group__link--level-1 page-group__link--selected page-group__link--level-1--selected">
                        <span class="page-group__text">${policyDetail.title}</span>
                    </span>
                <#else>
                    <@hst.link var="link" hippobean=policyDetail/>
                    <a class="page-group__link page-group__link--level-1" href="${link}">
                        <span class="page-group__text">${policyDetail.title}</span>
                    </a>
                </#if>
            </li>
        </#list>
    </ul>
</nav>