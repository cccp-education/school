<#include "header.ftl">

<#include "menu.ftl">

<#if (content.title)??>
    <div class="page-header">
        <h1><#escape x as x?xml>${content.title}</#escape></h1>
    </div>
<#else></#if>

    <div class="container mt-5 mb-5">
        <h4>A propos de moi</h4>
    <div class="row">
        <div class="col-md-6">
            <p>En tant que développeur, formateur et passionné de pédagogie basé à Paris, je m’engage à créer des ponts
                entre le monde du développement de logiciels et l’apprentissage continu.
                Mon expertise ne se limite pas seulement à la conception de solutions logicielles,
                mais s’étend également à la création d’un environnement d’apprentissage dynamique et efficace.</p>

            <p>Ma mission va au-delà du simple transfert de connaissances. En tant que formateur, j'accorde une
                importance particulière à l’étude et à la création de matériel pédagogique innovant.
                Mon objectif est de développer des ressources éducatives engageantes et adaptées,
                facilitant ainsi l’assimilation des concepts complexes par mes apprenants.</p>

            <p>L’animation de séances de formation est pour moi une opportunité de créer une expérience
                d’apprentissage interactive et stimulante. J’adopte des méthodes pédagogiques variées pour rendre l'acquisition de
                compétences aussi captivante que possible, encourageant ainsi un apprentissage actif et durable.</p>

            <p>L’évaluation continue est au cœur de mon approche. Je m’efforce de mettre en place des mécanismes
                d’évaluation variés, allant des projets pratiques aux évaluations formatives, afin de mesurer efficacement la
                compréhension et la progression de chaque apprenant. Les feedbacks personnalisés constituent un outil essentiel pour guider
                les apprenants vers l’amélioration continue.</p>

            <p>Mon engagement ne se limite pas à un public spécifique. Je suis passionné par l'accompagnement de divers profils,
                que ce soit des individus cherchant à retourner sur le marché de l'emploi, des professionnels en quête de formation
                continue tout au long de leur carrière, ou encore des apprenants en alternance. Mon approche vise à adapter les
                solutions pédagogiques de manière pratique et personnalisée, favorisant ainsi une transition réussie
                vers de nouvelles opportunités professionnelles.</p>

            <p>Ensemble, explorons les avenues de l’apprentissage, de la création de solutions pratiques à l'évolution
                constante des compétences, avec pour objectif commun de tracer des parcours de réussite dans le domaine du
                développement et au-delà.</p>
        </div>
    </div>
        <hr/>
    </div>
    <p>${content.body}</p>


<#include "footer.ftl">