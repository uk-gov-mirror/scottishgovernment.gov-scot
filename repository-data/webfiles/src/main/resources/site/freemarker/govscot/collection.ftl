<#include "../include/imports.ftl">

<@hst.manageContent hippobean=document/>

<h1 class="article-header">
${document.title}
</h1>

<p><@hst.html hippohtml=document.content/></p>

<#list document.groups as group>
    <#if group.groupTitle?has_content>
        <h2>${group.groupTitle}</h2>
    </#if>

    <@hst.html hippohtml=group.groupContent/>

    <#if group.order?? && group.order != 'none'>
        <#assign collectionItems = group.collectionItems?sort_by(group.order)/>
    <#else>
        <#assign collectionItems = group.collectionItems/>
    </#if>

    <ul class="no-bullets">
    <#list collectionItems as item>
        <li>
            <a href="<@hst.link hippobean=item/>" class="listed-content-item__link" title="${item.title}">
                <article class="listed-content-item__article ">
                    <header class="listed-content-item__header">
                        <div class="listed-content-item__meta">
                            <#if item.label??><p class="listed-content-item__label">${item.label}</p></#if>

                            <#if item.label == 'news'>
                                <p class="listed-content-item__date"><@fmt.formatDate value=item.publicationDate.time type="both" pattern="d MMM yyyy HH:mm"/></p>
                            <#else>
                                <p class="listed-content-item__date"><@fmt.formatDate value=item.publicationDate.time type="both" pattern="d MMM yyyy"/></p>
                            </#if>
                        </div>

                        <h3 class="gamma  listed-content-item__title" title="${item.title}">${item.title}</h3>
                    </header>
                </article>
            </a>
        </li>
    </#list>
    </ul>

</#list>