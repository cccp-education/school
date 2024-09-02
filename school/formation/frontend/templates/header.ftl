<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="google-site-verification" content="oTZL-J0lpqoMMfQvrhFBaPfG1gtsswFKz1RzHoMLgbo"/>
    <meta name="description" content="">
    <meta name="author" content="">
    <meta name="keywords" content="">

    <!--    <title>Talaria-Formation</title>-->
    <title><#if (content.title)??><#escape x as x?xml>${content.title}</#escape><#else>@Talaria.Formation</#if></title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script>
        tailwind.config = {
            darkMode: 'class',
            theme: {
                extend: {
                    colors: {
                        primary: '#3B82F6',
                        secondary: '#10B981',
                    }
                }
            }
        }
    </script>
    <link href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>css/asciidoctor.css" rel="stylesheet">
    <script src="https://kit.fontawesome.com/01a45d7f35.js" crossorigin="anonymous"></script>
    <link rel="shortcut icon" href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>favicon.ico">

</head>
