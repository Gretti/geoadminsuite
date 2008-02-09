<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<%@page contentType="text/html"%>
<%@ page language="java" %>
<html>
    <head>
        <title>GAS : TODO List</title>
        <META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=iso-8859-1">
        <META HTTP-EQUIV="Pragma" CONTENT="No-cache">
        <META HTTP-EQUIV="Cache-Control" CONTENT="no-cache">
        <META HTTP-EQUIV="Expires" CONTENT=0>
        <link rel="stylesheet" href="css/gas.css" type="text/css">
    </HEAD>
    <body class='body2'>
        <table class='tablea' width='75%' border="1" cellspacing='0' cellpadding='5' align=center>
            <tr><th align="center">Description</th></tr>
            <tr><td class="tinygrey">
                    Faire un editeur de mapfile manuel.
            </td></tr>
            <tr><td class="tinygrey">
                    <i>Changer les trees pour faire marcher sous FF (cf GeoSASS ou arbre cartoweb).</i> OK
            </td></tr>
            <tr><td>
                    Clarifier les sources de données: chemins/servers locaux au serveur ou au client ?
            </td></tr>
            <tr><td>
                    En cas de source de données inaccessible, l'indiquer sous forme de note(pas de postgis par ex)
            </td></tr>
            <tr><td>
                    Mettre des pseudo popup a la place des fenetres popup.
            </td></tr>
            <tr><td>
                    En cas de couche non affichée en quicklook, essayer de chercher si le fichier local est accessible avec l'utilisateur MS, ou indiquer dans un message d'erreur de tester cela
            </td></tr>
            <tr><td>
                    Revoir la gestion de Kaboum 4, ajax pour eviter de recharger la page ou appel a KaboumServer pour charger la liste de nouvelles couches.
            </td></tr>
            <tr><td>
                    Trier les données par ordre alphabétique dans le catalogue.
            </td></tr>
            <tr><td>
                    I18N : revoir toutes les pages. Faire plusieurs fichiers d'internationalisation (1 général + 1 par rubrique).
            </td></tr>
            <tr><td>
                    Ranger les sources (html dans répertoire HTML).
            </td></tr>
            <tr><td>
                    Remplacer LogEngine par Logger Java. Gérer les niveaux et informations a Logger.
            </td></tr>
            <tr><td>
                    Gestion des exceptions : gestion plus fine des exceptions système (error) et des exception applicatives (ActionMessage). Eviter les stackTrace.
            </td></tr>
            <tr><td>
                    Revoir la gestion des projections sur la base de EPSG.
            </td></tr>
            <tr><td>
                    Intégrer GeoTools 2.xx.
            </td></tr>
            <tr><td>
                    Revoir le modèle objets : scinder les objets en beans + managers.
            </td></tr>
            <tr><td>
                    Faire marcher tout ça sous IE7.
            </td></tr>
            <tr><td>
                    <strike>Changer le colorchooser java par celui de cartoweb.</strike> (il te plait pas le colorChooser YUI pur DHTML ?! ;-))
                    <br><i>si si, tout bon, j'enleve mon commentaire... ;-)</i>
            </td></tr>
            <tr><td class="tinygrey">
                    <i>Faire fonctionner datasources ORACLE.</i> OK
            </td></tr>
            <tr><td class="tinygrey">
                    <i>Modification de l'ordre des couches sans rafraichissement de la page entière (DHTML + AJaX).</i> OK
            </td></tr>
        </table>
    </body>
</html>
