#!/bin/bash
################################################################################
# Script permettant de lancer le recalage des trajets en masse (UU par UU)
# , en se basant sur un fichier texte contenant la liste des UU a traiter
# 
#_________________________
# Les etapes du traitement:
# 
# creer la table de stats
# Lister les donnees contenues dans le fichier TXT
# Copier le rar de l''UU, extraire les données
# controler la presence des fichiers: si ko -> log en base avec bon message
# controler les attributs des trajets: si ko -> log en base avec bon message
# insertion en base de l''UU en cours, date de debut de script, date de fin
# appeler process_trajet avec les bons parametres
# insertion en base de la date de fin pour l''UU en cours
# affichage bilan du traitement.
# _____
# TODO:
# 
# dans la notice, expliquer comment lire le log en temps reel: tail -f.
#
################################################################################

#####################Variables a adapter #######################################
# parametres de connexion de la base de donnée de travail
DBCONN="-d bva -p 5432"

# Chemin dans lequel se trouvent les RAR contenant les trajets
UU_PATH=/mnt/data/UU10-new
#UU_PATH=/mnt/data/UU-rural/
#UU_PATH=//Users/nicolas/Projets/BVA/data/control/UU10-new/

# Chemin dans lequel se trouvent les donnees de 2010
#UU_PATH_2010=/mnt/data/UU-2010/UU_10-100
UU_PATH_2010=/Users/nicolas/Projets/BVA/data/control/UU-2010/UU_10-100

# Chemin ou sont copiés les données issues du traitement
OUT_PATH=/Users/nicolas/Projets/BVA/data/control/results-new
#OUT_PATH=/mnt/data/results-new
#OUT_PATH=/mnt/data/results-new/rural

#Repertoire de travail temporaire ou sont copiés et décompressés les RAR contenant les trajets
TMP_DIR=/tmp

#####################Variables privees, ne pas toucher ##########################
CMD_STATUS=

# Liste des attributs pour les UU de type > 100 000
UU100_ATTR_LIST=(
"ORDRE: Integer" 
"NUMDEPL: Integer" 
"SENS: Integer" 
"MODETR: String" 
"MODEPRINC: String" 
"IDDEPL: String" 
"CODE_GS: String"
"POSTE: String" 
"ID: String" 
"LGTRON: Real" 
"PID: Real"
)	

__UU100_ATTR_LIST=(
"ORDRE: Integer" 
"NUMDEPL: Integer" 
"SENS: Integer" 
"MODETR: String" 
"MODEPRINC: String" 
"IDDEPL: String" 
"CODE_GS: String" 
"NUMTRAJET: Integer" 
"POSTE: String" 
"ID: String" 
"LGTRON: Real" 
"PID: Real" 
"POIDS: Real"
)	

###################################### Fonctions ###############################

# teste si les attributs du shapefile sont bons.
# retourne 0=ok, 1=ko
testattr() {
	STAT_DESC=
	
	ogrinfo -al -so $1 > attrs__.txt
	for i in "${UU100_ATTR_LIST[@]}" ; do
		cat attrs__.txt | grep "$i" > /dev/null
		if [[ $? -gt 0 ]] ; then
			STAT_DESC=$STAT_DESC"attr:: $i ::manquant. "
		fi
	done
	
	rm -f attrs__.txt
	echo $STAT_DESC
	# si des attributs manquent, insertion en base + message d'erreur
	if [[ -n $STAT_DESC ]] ; then  
	 	psql $DBCONN -c "update stats set proc_end=now(), status_ok=false, description='$STAT_DESC'"
		return 1
	else 
		return 0
	fi
	
}

# test tout pourri du status code passé en params
testcmdstatus() {
	if [[ $1 == 0 ]] ; then
		CMD_STATUS="...................OK"
	else 
		CMD_STATUS="...................KO"
	fi
	return 0
}


################################################################################
	
############################### MAIN ###########################################

#### Test des parametres en entree
if [[ $# -lt 2 ]] ; then
	echo usage: "$0 <liste des UU> <repertoire de sortie>
	
		<liste des UU>        : le chemin vers le fichier contenant la liste des UU a traiter
		<repertoire de sortie>: le chemin ABSOLU vers le repertoire de sortie ou ecrire les resultats"
	exit 1
fi

### Test de la validité des parametres en entree
if [[ ! -f $1 ]] ; then
	echo le fichier listant les UU, $1, n"'"existe pas ou n"'"est pas trouvable
	echo "FIN du traitement."
	exit 1
fi

if [[ ! -d $2 ]] ; then
	echo le répertoire de sortie, $2, n"'"existe pas ou n"'"est pas trouvable
	echo "FIN du traitement."
	exit 1
fi

BEGIN="$(date +%s)"
T0="$(date +%s)"

echo "__________________________________________________________________________"
echo "traitement commence a: $(date) sur la base $DBCONN"
echo 

# repertoire courant, on le sauve pour plus tard
CUR_DIR=`pwd`

# preparation du repertoire de sortie: cree les sous rep ok et ko, pour stocker
# les resultats en fonction de la qualité de traitement.
if [[ ! -d $OUT_PATH/ok ]] ; then
    echo "creation du sous repertoire de sortie: $OUT_PATH/ok"
    mkdir $OUT_PATH/ok
fi

if [[ ! -d $OUT_PATH/ko ]] ; then
    echo "creation du sous repertoire de sortie: $OUT_PATH/ko"
    mkdir $OUT_PATH/ko
fi

#for UUCODE_FILE in `cat $1`; do
while read UUCODE_FILE ; do
	# On ignore l'UU si elle est en commentaire dans le fichier
	if [[ `echo ${UUCODE_FILE} | cut -c1` == '#' ]] ; then
		echo "UU en commentaire : "`echo ${UUCODE_FILE} | cut -c2-`
		continue
	fi

	# renommage sans espaces
	OLD_UUCODE_FILE=${UUCODE_FILE}
	UUCODE_FILE=`echo ${OLD_UUCODE_FILE}|sed -e 's/ /_/g'`
	echo "nouvelle variable: $UUCODE_FILE"
	
	mv $UU_PATH/"${OLD_UUCODE_FILE}" $UU_PATH/${UUCODE_FILE}
	
	case ${UUCODE_FILE} in
		2[a]*)
			echo "Traitement supplementaire pour la Corse"
			OLD_UUCODE_FILE=${UUCODE_FILE}
			UUCODE_FILE="2A"`echo ${UUCODE_FILE} | cut -c3-`

			# Changement du RAR en 2AXXX si ce n'est pas deja fait
			if [ ! -f $UU_PATH/${UUCODE_FILE} ] ; then
				mv $UU_PATH/${OLD_UUCODE_FILE} $UU_PATH/${UUCODE_FILE}  
			fi
			;;
		2[b]*)
			echo "Traitement supplementaire pour la Corse"
			UUCODE_FILE="2B"`echo ${UUCODE_FILE} | cut -c3-`

			# Changement du RAR en 2BXXX si ce n'est pas deja fait
			if [ ! -f $UU_PATH/${UUCODE_FILE} ] ; then
				mv $UU_PATH/${OLD_UUCODE_FILE} $UU_PATH/${UUCODE_FILE}  
			fi
			;;
	esac
	
	# On ne veut que les 5 premiers caractères de chaque ligne -> code UU uniquement
	UUCODE=`echo $UUCODE_FILE | cut -c1-5`
	# On ne veut que le type du fichier compresse 'zip' ou 'rar' et on force le lower case
	UUCODE_TYPE=`echo $UUCODE_FILE | rev | cut -c1-3 | rev | tr -s '[:upper:]' '[:lower:]'`
	# On ne veut que le nom du fichier sans l'extension
	UUCODE_BASENAME=`echo $UUCODE_FILE | rev | cut -c5- | rev`

	echo ""
	echo "_________________Traitement de l'unite urbaine: $UUCODE _____________"
	echo "Fichier de l'unite urbaine : $UUCODE_FILE, code: $UUCODE"
	echo "Format de compression du fichier : $UUCODE_TYPE"

	# insertion en base de cette UU, pour controle.
	psql $DBCONN -c "delete from stats where code_uu='${UUCODE}'"
	psql $DBCONN -c "insert into stats(code_uu, proc_start) values ('${UUCODE}', now())"
	
	# suppression preventive d'un fichier precedent
	rm -rf $TMP_DIR/$UUCODE*
	# copie et traitement du fichier compresse
	echo "copie du fichier: ${UUCODE_FILE}"
	cp "$UU_PATH/${UUCODE_FILE}" $TMP_DIR/
	# ${UUCODE}.rar devient ${UUCODE_FILE}
	# decompression du fichier suivant son type
	if [[ $UUCODE_TYPE == "rar" ]] ; then
		CMD_DECOMPRESS="unrar x -o+ ${UUCODE_FILE}"
	else
		CMD_DECOMPRESS="unzip ${UUCODE_FILE} -d $UUCODE_BASENAME"
	fi
	cd $TMP_DIR && $CMD_DECOMPRESS

	# no filename expansion in [[ ]] so use old [ ] instead
	#if [ ! -f $UUCODE/[Rr]esultats/trajetssimul.shp ] || [ ! -f $UUCODE/[Rr]esultats/trajetssimul.dbf ] || [ ! -f $UUCODE/[Rr]esultats/trajetssimul.shx ] ; then
	# conversion de la variable de minuscule, pour gerer le cas de la Corse.
	CORSE=`echo $UUCODE | tr '[:upper:]' '[:lower:]'`
	if [ ! -f $UUCODE*/trajetssimul.shp -o ! -f $UUCODE*/trajetssimul.dbf -o ! -f $UUCODE*/trajetssimul.shx -a \(! -f $CORSE*/trajetssimul.shx -o ! -f $CORSE*/trajetssimul.shp -o ! -f $CORSE*/trajetssimul.dbf\) ]; then
		echo "Les fichiers shapefile des trajets (${UUCODE}.../trajetssimul.shp, .dbf, .shx) n'existent pas ou n'ont pas le bon nom."
		echo "UU $UUCODE non traitée"
		# insertion dans la table de stats 
		psql $DBCONN -c "update stats set proc_end=now(), status_ok=false, description='Fichiers de trajets non trouves dans l archive ${UUCODE_FILE}' where code_uu='${UUCODE}'"
		ls -al ${UUCODE}*
		#rm -rf ${UUCODE}*
	else
		# suite du traitement normal: les fichiers sont présents. Formats valides pour les attributs ?
		testattr ${UUCODE}*/trajetssimul.shp
		
		if [[ $? -eq 1 ]] ; then
			echo "Attributs incorrects. La table de stat contient les raisons de l'invalidité..."
			echo "arret du traitement pour l'UU $UUCODE."
			#nettoyage de l'archive.
			echo "nettoyage du rar..."
			cd $TMP_DIR
			rm -rf ${UUCODE}*
		else
			# Trajectoire 2006 ok passons au trajet 2010
			echo "Verification des trajets 2006 de l'UU ${UUCODE}........ OK"

			# Unrar du fichier trajectoire de 2010
			UUCODE_FILE_2010=$UU_PATH_2010/${UUCODE}/${UUCODE}.rar
			TMP_DIR_2010=$TMP_DIR/2010_${UUCODE}
			rm -rf $TMP_DIR_2010 && mkdir -p $TMP_DIR_2010
			echo "Fichier archive contenant les trajets 2010 : ${UUCODE_FILE_2010}"
			unrar x -inul ${UUCODE_FILE_2010} $TMP_DIR_2010
			echo "Decompression des trajets 2010 dans le dossier ${TMP_DIR_2010}"

			# Fichier shapefile 2010 suivant plusieurs cas possible
			TRAJET_FILE_2010="$TMP_DIR_2010/Trajetssimul${UUCODE}_Merge"
			SHAPE_FILE_2010="${TRAJET_FILE_2010}.shp"
			if [ ! -f $SHAPE_FILE_2010 ] ; then
				TRAJET_FILE_2010="$TMP_DIR_2010/Trajetssimul${UUCODE}"
				SHAPE_FILE_2010="${TRAJET_FILE_2010}.shp"
			fi
			if [ ! -f $SHAPE_FILE_2010 ] ; then
				TRAJET_FILE_2010="$TMP_DIR_2010/trajetssimul${UUCODE}"
				SHAPE_FILE_2010="${TRAJET_FILE_2010}.shp"
			fi
			if [ ! -f $SHAPE_FILE_2010 ] ; then
				TRAJET_FILE_2010="$TMP_DIR_2010/trajetssimul${UUCODE}_Merge"
				SHAPE_FILE_2010="${TRAJET_FILE_2010}.shp"
			fi

			echo "Fichier Shapefile 2010 de l'uu : "$SHAPE_FILE_2010

			#if [ ! -f $SHAPE_FILE_2010 ] || [ ! -f $TMP_DIR_2010/Trajetssimul${UUCODE}.dbf ] || [ ! -f $TMP_DIR_2010/Trajetssimul${UUCODE}.shx ] ; then
			if [ ! -f $SHAPE_FILE_2010 ] || [ ! -f ${TRAJET_FILE_2010}.dbf ] || [ ! -f ${TRAJET_FILE_2010}.shx ] ; then
				echo "Les fichiers shapefile des trajets 2010 ($SHAPE_FILE_2010, .dbf, .shx) n'existent pas ou n'ont pas le bon nom."
				#echo "UU $UUCODE non traitée"
				echo "Le traitement se poursuit"
				# insertion dans la table de stats 
		#		psql $DBCONN -c "update stats set proc_end=now(), status_ok=false, description='Fichiers de trajets 2010 non trouves dans l archive ${UUCODE_FILE}' where code_uu='${UUCODE}'"
				ls -al $TMP_DIR_2010
				sleep 1
				#rm -rf $TMP_DIR_2010
			fi
				# suite du traitement normal: les fichiers sont présents. Formats valides pour les attributs ?
# 				testattr ${SHAPE_FILE_2010}
				#echo "Tout va bien"

				#if [[ $? -eq 1 ]] ; then
				#	echo "Attributs incorrects pour les trajets 2010. La table de stat contient les raisons de l'invalidité..."
				#	echo "arret du traitement pour l'UU $UUCODE."
				#	#nettoyage de l'archive.
				#	echo "nettoyage du rar..."
				#	rm -rf $TMP_DIR_2010
				#else
					# tous les attributs sont OK, on balance le traitement:

					# Fichier contenant les ponderations
					PONDERATION_FILE="$UU_PATH_2010/${UUCODE}/base_individus_${UUCODE}.txt"
					echo "Fichier de ponderation de l'uu : "$PONDERATION_FILE
					# Fichier shapefile
					SHAPE_FILE="$TMP_DIR/$UUCODE*/trajetssimul.shp"
					echo "Fichier Shapefile de l'uu : "$SHAPE_FILE

					# insertion en base de cette UU en cours
					echo "insertion en base de l'uu en cours: ${UUCODE}"
					
					echo "Lancement du traitement SQL..."
					cd $CUR_DIR
					sh process_trajets.sh $SHAPE_FILE $2 $PONDERATION_FILE $SHAPE_FILE_2010
		#			# lancement sur le bon rep: ameliorer pour ne pas tenir compte de la casse.
		#			if [ -d $TMP_DIR/$UUCODE/resultats ] ; then
		#				#echo "sh process_trajets.sh $TMP_DIR/$UUCODE/resultats/trajetssimul.shp $2"
		#				sh process_trajets.sh $TMP_DIR/$UUCODE*/resultats/trajetssimul.shp $2
		#			else
		#				#echo "sh process_trajets.sh $TMP_DIR/$UUCODE/Resultats/trajetssimul.shp $2"
		#				sh process_trajets.sh $TMP_DIR/$UUCODE*/Resultats/trajetssimul.shp $2
		#			fi
					STATUS_CODE=$?
					
					echo "status code: $STATUS_CODE"
					
					if [[ $STATUS_CODE -eq 0 ]] ; then
						DESC_ERREUR=
						DB_STATUS_OK=true
					elif [[ $STATUS_CODE -eq 100 ]]; then
					    DESC_ERREUR="Donnees de trajets incorrectes: impossible de reconstruire des trajets complets sous forme de MULTILINESTRING"
						DB_STATUS_OK=false
					elif [[ $STATUS_CODE -eq 200 ]] ; then
					    DESC_ERREUR="process_trajet en erreur: chargement des trajets impossible: $SHAPE_FILE"
						DB_STATUS_OK=false
					elif [[ $STATUS_CODE -eq 201 ]] ; then
					    DESC_ERREUR="process_trajet en erreur: chargement de la ponderation impossible: $PONDERATION_FILE"
						DB_STATUS_OK=false
					else 
					    DESC_ERREUR="process_trajet en erreur: erreur inattendue. (code=$STATUS_CODE)"
						DB_STATUS_OK=false
					fi
					#insertion en base de la fin du traitement
					psql $DBCONN -c "update stats set proc_end=now(), status_ok=${DB_STATUS_OK}, description='${DESC_ERREUR}' where code_uu='${UUCODE}'"
					
					#nettoyage de l'archive.
					echo "nettoyage du rar..."
					# Archive trajet 2006
					cd $TMP_DIR
					rm -rf ${UUCODE}*
					testcmdstatus $?
					# Archive trajet 2010
					rm -rf $TMP_DIR_2010
					testcmdstatus $?
					
					#zip et renommage des fichiers de sortie uniquement si process OK
					if [[ $DB_STATUS_OK == "true" ]] ; then
						echo "zip du resultat..."
						cd $2
						zip -q uu_${UUCODE}.zip trajet_*.* *.csv
						if [[ $? == 0 ]] ; then
							echo "...................OK"
							echo "effacement des fichiers de sortie..."
							rm -f trajet_*.* && rm -f *.csv
							testcmdstatus $?
							echo $CMD_STATUS
							
							# copie du resultat vers l'hote de destination, en fonction du pourcentage de longueur:
							# si > 95, copie dans rep ok, sinon dans rep ko.
							# utilisation de la comparaison en SQL car shell sait pas faire
							PRC_LONG=`psql $DBCONN -At -c "select prc_long > 95 from stats where code_uu='$UUCODE'"`
							if [ $PRC_LONG = 't' ] ; then
							    echo "deplacement du resultat dans le rep OK :)"
							    mv uu_${UUCODE}.zip $OUT_PATH/ok
							else 
							    echo "mauvaises stat, deplacement du resultat dans le rep KO :(("
							    mv uu_${UUCODE}.zip $OUT_PATH/ko
							fi
							
							if [[ $? == 0 ]] ; then
								echo "...................OK"
								echo "effacement du zip de resultat..."
								rm -f uu_${UUCODE}.zip
								testcmdstatus $?
								echo $CMD_STATUS
							else 
								echo "...................KO"
							fi
						else 
							echo "...................KO"
						fi
					fi
					# message de fin de traitement
					END="$(date +%s)"
					echo "temps de traitement UU $UUCODE: $(expr $END - $T0) s."
				#fi
			#fi
		fi
	fi
done < $1 # end loop lecture des codes UU a traiter

END="$(date +%s)"

echo "temps total: $(expr $END - $T0) s."
echo "---------traitement termine a:  $(date)------"
exit 0
