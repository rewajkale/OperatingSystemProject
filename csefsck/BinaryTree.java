package csefsck;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class BinaryTree {
String cfile="E://rewa//nyu//os//os assignment//FS//fusedata.";
 BinaryTree lnode;
 BinaryTree rnode;
 BufferedReader br=null;
	String l = "";
	String value[];
	int root;
 public void readSuperBlock()
	{
		try {
			br = new BufferedReader(new FileReader(cfile+"0"));
			l = br.readLine();
			value=l.split(",");
			root=Integer.parseInt(value[5].substring(value[5].indexOf(":")+1));
			//System.out.println(root);
		}
			catch (FileNotFoundException e) {} catch (IOException e) {} 
		    catch (NumberFormatException e){
				System.out.println("Block number pointing to root is not an integer");
			}
	}
 public void traceTree()
 {
	 
 }
}
