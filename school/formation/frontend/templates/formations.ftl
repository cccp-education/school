<#include "header.ftl">

<#include "menu.ftl">

<#if (content.title)??>
    <div class="page-header">
        <h1><#escape x as x?xml>${content.title}</#escape></h1>
    </div>
<#else></#if>

    <div class="container mt-5 mb-5">
        <h4>Catalogue des formations</h4>
    <div class="row">
        <div class="col-md-6">
<#--liste des fomations ici -->
        </div>
    </div>
        <hr/>
    </div>
    <p>${content.body}</p>


<#include "footer.ftl">