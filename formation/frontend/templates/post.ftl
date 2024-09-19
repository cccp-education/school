<#include "header.ftl">

	<#include "menu.ftl">

	<#if (content.title)??>
	<div class="page-header">
		<h1><#escape x as x?xml>${content.title}</#escape></h1>
	</div>
	<#else></#if>

	<p><em>${content.date?string("dd MMMM yyyy")}</em></p>

	<p>${content.body}</p>

	<section id="disqus_thread"></section>
	<script type="text/javascript">
		var disqus_identifier = '${content.uri}';

		(({disqus_shortname, document}) => {
			injectScript('//' + disqus_shortname + '.disqus.com/embed.js');
			injectScript('//' + disqus_shortname + '.disqus.com/count.js');

			function injectScript(url) {
				const s = document.createElement('script');
				s.async = true;
				s.src = url;
				(document.getElementsByTagName('head')[0] || document.getElementsByTagName('body')[0]).appendChild(s);
			}
		})({'disqus_shortname': 'TODO', 'document': document});
	</script>
	<noscript>Please enable JavaScript to view the <a href="http://disqus.com/?ref_noscript">comments powered by Disqus.</a></noscript>
	<hr/>
<#include "footer.ftl">