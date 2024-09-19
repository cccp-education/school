<#include "header.ftl">
<#include "menu.ftl">
<!-- <a href="${content.rootpath}${config.archive_file}">Archives</a> -->
<p>Les anciens post sont disponibles içi : <a href="${content.rootpath}${config.archive_file}">archive</a>.</p>
<div class="page-header"></div>
<div class="row">
    <#list posts as post>
     <#if (post.status == "published")>
        <div class="panel panel-default col-md-7">
            <div class="panel-body">
                    <a href="${post.uri}">
                        <h3><#escape x as x?xml>${post.title}</#escape></h3></a>
                    <p>${post.date?string("dd MMMM yyyy")}</p>
                    <#if post.summary??>
                        <div class="well">${post.summary}</div><#else></#if>
            </div>
        </div>
        </#if>
    </#list>
</div>
<hr/>
<#include "footer.ftl">