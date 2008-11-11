package org.geogurus.tools.util;



import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;



public class TabByte

{

	 /**

	  * @param int [] : Tableau des valeurs � convertir en un tableau de byte

	  * @return byte [] : tableau de byte representant le tableau de double

	  */

	public static byte []tabIntToByte(int []tableau) throws Exception

	{

		ByteArrayOutputStream bout = new ByteArrayOutputStream(tableau.length * 4);   // int sont cod�s sur 4 octets

		DataOutputStream data_out = new DataOutputStream(bout);



		for(int i=0; i<tableau.length; i++)

		{

			data_out.writeInt(tableau[i]);

		}

		byte []resultat = bout.toByteArray();



		data_out.close();



		return resultat;

	}



	 /**

	  * @param double [] : Tableau des valeurs � convertir en un tableau de byte

	  * @return byte [] : tableau de byte representant le tableau de double

	  */

	public static byte []tabDoubleToByte(double []tableau) throws Exception

	{

		ByteArrayOutputStream bout = new ByteArrayOutputStream(tableau.length * 8);

		DataOutputStream data_out = new DataOutputStream(bout);



		for(int i=0; i<tableau.length; i++)

			data_out.writeDouble(tableau[i]);



		byte []resultat = bout.toByteArray();



		data_out.close();



		return resultat;

	}



	 /**

	  * @param long [] : Tableau des valeurs � convertir en un tableau de byte

	  * @return byte [] : tableau de byte representant le tableau de double

	  */

	public static byte []tabLongToByte(long []tableau) throws Exception

	{

		ByteArrayOutputStream bout = new ByteArrayOutputStream(tableau.length * 8);

		DataOutputStream data_out = new DataOutputStream(bout);



		for(int i=0; i<tableau.length; i++)

			data_out.writeLong(tableau[i]);



		byte []resultat = bout.toByteArray();



		data_out.close();



		return resultat;

	}



	 /**

	  * @param byte [] : Tableau de byte � convertir en un tableau de long

	  * @return byte [] : tableau de long representant le tableau de byte

	  */

	public static long []tabByteToLong(byte []tableau) throws Exception

	{

		int taille = tableau.length / 8;

		long []resultat = new long[taille];



		ByteArrayInputStream bin = new ByteArrayInputStream(tableau);

		DataInputStream objin = new DataInputStream(bin);



		for(int i=0; i<taille; i++)

			resultat[i] = objin.readLong();

		objin.close();



		return resultat;

	}



	 /**

	  * @param byte [] : Tableau de byte � convertir en un tableau de double

	  * @return byte [] : tableau de double representant le tableau de byte

	  */

	public static double []tabByteToDouble(byte []tableau) throws Exception

	{

		int taille = tableau.length / 8;

		double []resultat = new double[taille];



		ByteArrayInputStream bin = new ByteArrayInputStream(tableau);

		DataInputStream objin = new DataInputStream(bin);



		for(int i=0; i<taille; i++)

			resultat[i] = objin.readDouble();

		objin.close();



		return resultat;

	}



	 /**

	  * @param byte [] : Tableau de byte � convertir en un tableau de double

	  * @return int [] : tableau de int representant le tableau de byte

	  */

	public static int []tabByteToInt(byte []tableau) throws Exception

	{

		int taille = tableau.length / 4;

		int []resultat = new int[taille];



		ByteArrayInputStream bin = new ByteArrayInputStream(tableau);

		DataInputStream objin = new DataInputStream(bin);



		for(int i=0; i<taille; i++)

			resultat[i] = objin.readInt();

		objin.close();



		return resultat;

	}

}