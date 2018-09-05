<#include "../../include/imports.ftl">
<@hst.webfile var="iconspath" path="/assets/images/icons/icons.stack.svg"/>

<section class="topics">
  <div class="az-list">
    <#list topicsByLetter as letter>
      <div class="grid"><!--
        --><div class="grid__item two-twelfths medium--one-ninth az-list__chunkName" id="az-list__${letter.letter}">${letter.letter}</div><!--
        --><div class="grid__item ten-twelfths medium--seven-ninths az-list__chunk">
          <ul class="az-list__list grid"><!--
            <#list letter.topics as topic>
              --><li class="az-list__item grid__item ">
                <div class="topic">
                  <h2 class="gamma topic__title">
                    <a href="<@hst.link hippobean=topic />">${topic.title}</a>
                  </h2>

                  <p>See all related:</p>

                  <div class="topic__buttons">
                    <a class="button button--xsmall button--primary button--pill button--margin-right" href="<@hst.link path='/policies/?topics=${topic.title}'/>">
                        <svg class="svg-icon  mg-icon  mg-icon--absolute  mg-icon--medium--material  mg-icon--right">
                            <use xmlns:xlink="http://www.w3.org/1999/xlink" xlink:href="${iconspath}#sharp-chevron_right-24px"></use>
                        </svg>
                        Policies
                    </a>
                    <a class="button button--xsmall button--primary button--pill button--margin-right" href="<@hst.link path='/news/?topics=${topic.title}'/>">
                        <svg class="svg-icon  mg-icon  mg-icon--absolute  mg-icon--medium--material  mg-icon--right">
                            <use xmlns:xlink="http://www.w3.org/1999/xlink" xlink:href="${iconspath}#sharp-chevron_right-24px"></use>
                        </svg>
                        News
                    </a>
                    <a class="button button--xsmall button--primary button--pill button--margin-right" href="<@hst.link path='/publications/?topics=${topic.title}'/>">
                        <svg class="svg-icon  mg-icon  mg-icon--absolute  mg-icon--medium--material  mg-icon--right">
                            <use xmlns:xlink="http://www.w3.org/1999/xlink" xlink:href="${iconspath}#sharp-chevron_right-24px"></use>
                        </svg>
                        Publications
                    </a>
                  </div>
                </div>
              </li><!--
            </#list>
            <#-- end letter.topics loop -->
          --></ul>
        </div><!--
      --></div>
    </#list>
    <#-- end topicsByLetter loop -->
  </div>
</section>
