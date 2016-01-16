package csefsck;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.io.FileNotFoundException;

class Csefsck {
	static String cfile="C://Users//Admin//Desktop//FS//fusedata.";
	BufferedReader br=null;
	String l = "";
	static String value[];
	int root, freeStart, freeEnd, maxBlocks;
	static int delFile=0;
	static int blocksize=4096;
	static ArrayList<Integer> arrd=new ArrayList<Integer>();
	static ArrayList<Integer> arrf=new ArrayList<Integer>();
	static ArrayList<Integer> arrs=new ArrayList<Integer>();
	public void readSuperBlock()
	{
		try {
			br = new BufferedReader(new FileReader(cfile+"0"));
			l = br.readLine();
			value=l.split(",");
			root=Integer.parseInt(value[5].substring(value[5].indexOf(":")+1).trim());
			freeStart=Integer.parseInt(value[3].substring(value[3].indexOf(":")+1).trim());
			freeEnd=Integer.parseInt(value[4].substring(value[4].indexOf(":")+1).trim());
			maxBlocks=Integer.parseInt(value[6].substring(value[6].indexOf(":")+1, value[6].indexOf("}")).trim());
			int i=root;
			editHash(i,"d");
		}catch (NumberFormatException e){System.out.println("Block number pointing to root is not an integer");}catch (FileNotFoundException e) {} catch (IOException e) {} 
	}
public void editHash(int i,String key)
{
	try{
		if(key=="d")
		{
			arrd.add(i);
			br = new BufferedReader(new FileReader(cfile+""+i+""));
			l = br.readLine();
			value=l.split(",");
			for(String str:value)
			{
				if(str.contains("{f:")||str.contains(" f:"))
				{
					int ind=str.indexOf("f:")+2;
					while(str.charAt(ind)!=':')
						ind++;
					ind++;
					if(str.contains("}"))
					    i=Integer.parseInt(str.substring(ind,str.indexOf('}')));
					else
						i=Integer.parseInt(str.substring(ind));
					editHash(i, "f");
				}
				else if(str.contains(" d:")||str.contains("{d:"))
				{
					int ind=str.indexOf("d:")+2;
					if((str!=value[value.length-1])&&(str.contains("."))||(str.contains("..")))
					{	continue;}
					else if((!(str.contains("."))||(str.contains(".."))))
					{
						while(str.charAt(ind)!=':')
							ind++;
						ind++;
						if(str.contains("}"))
						    i=Integer.parseInt(str.substring(ind,str.indexOf('}')));
						else
							i=Integer.parseInt(str.substring(ind));
						editHash(i,"d");
					}
					else if((str==value[value.length-1]))
						break;
				}
			}
		}
		else if(key=="f")
		{  
			arrf.add(i);
		}
		else if(key=="s")
		{	arrs.add(i);}
	}catch (FileNotFoundException e) {} catch (IOException e) {} 
}
public void devIdcheck()
{
	String devID = null;
	try {
		br = new BufferedReader(new FileReader(cfile+"0"));
		l = br.readLine();
		value=l.split(",");
		devID=value[2].substring(value[2].indexOf(":")+1);
	}catch (FileNotFoundException e) {} catch (IOException e) {} 
	if(devID.equals("20"))
	{
		System.out.println("Device ID is correct");
	}
	else
	{
		System.out.println("Incorrect Device ID");
		value[2]=value[2].replace(value[2].substring(value[2].indexOf(":")+1), "20");
		String str = "";
		for(String s:value)
		{
			if(s.endsWith("}")){str=str+s;break;}
			else
				str=str+s+",";
		}
		try {
			PrintWriter p=new PrintWriter(cfile+"0");
			p.print(str);
			p.close();
		} catch (FileNotFoundException e) {}
	}
}
public void checkTime()
{
	long currtime=System.currentTimeMillis()/1000;
	try {
		br = new BufferedReader(new FileReader(cfile+"0"));
		l = br.readLine(); 
		value=l.split(",");
		if(Long.parseLong(value[0].substring(value[0].indexOf(":")+1).trim())>currtime)
		{
			value[0]=value[0].replace(value[0].substring(value[0].indexOf(":")+1),""+currtime+"");
			String str = "";
			for(String s:value)
			{
				if(s.endsWith("}")){str=str+s;break;}
				else
					str=str+s+",";
			}
			PrintWriter p=new PrintWriter(cfile+"0");
			p.print(str);
			p.close();
		}
		else
			{System.out.println("Creation time is not in future");}
		checkTimeDir(arrd);
		checkTimeDir(arrf);
		checkTimeDir(arrs);
	}catch (FileNotFoundException e) {} catch (IOException e) {} 
}
public void checkTimeDir(ArrayList<Integer> arr)
{
	Long currtime=System.currentTimeMillis()/1000;
	int i=0;
	if(arr==arrf)
		i=1;
	if(arr!=null)
	{
	try {
		for(int j:arr)
		{
			br = new BufferedReader(new FileReader(cfile+j+""));
			l = br.readLine();
			value=l.split(",");
			if(Long.parseLong(value[4+i].substring(value[4+i].indexOf(":")+1).trim())>currtime)
			{
				value[4+i]=value[4+i].replace(value[4+i].substring(value[4+i].indexOf(":")+1),""+currtime+"");
				String str = "";
				for(String s:value)
				{
					if(s.endsWith("}")){str=str+s;break;}
					else
						str=str+s+",";
				}
				PrintWriter p=new PrintWriter(cfile+""+j);
				p.print(str);
				p.close();
			}
			else
				{System.out.println("Access time is not in future");}
			if(Long.parseLong(value[5+i].substring(value[5+i].indexOf(":")+1).trim())>currtime)
			{
				value[5+i]=value[5+i].replace(value[5+i].substring(value[5+i].indexOf(":")+1),""+currtime+"");
				String str = "";
				for(String s:value)
				{
					if(s.endsWith("}")){str=str+s;break;}
					else
						str=str+s+",";
				}
				PrintWriter p=new PrintWriter(cfile+""+j);
				p.print(str);
				p.close();
			}
			else
				{System.out.println("Creation time is not in future");}
			if(Long.parseLong(value[6+i].substring(value[6+i].indexOf(":")+1).trim())>currtime)
			{
				value[6+i]=value[6+i].replace(value[6+i].substring(value[6+i].indexOf(":")+1),""+currtime+"");
				String str = "";
				for(String s:value)
				{
					if(s.endsWith("}")){str=str+s;break;}
					else
						str=str+s+",";
				}
				PrintWriter p=new PrintWriter(cfile+""+j);
				p.print(str);
				p.close();
			}
			else
				{System.out.println("Modification time is not in future");}
		}
	}catch (FileNotFoundException e) {} catch (IOException e) {} 
	}
}
public void freeBlockCheck()
{
	System.out.println("Checking block list");
	ArrayList<Integer> arr=new ArrayList<Integer>();
	for(int i=freeStart;i<=freeEnd;i++)
	{
		int ind = 0;
		try {
			br = new BufferedReader(new FileReader(cfile+""+i));
			l = br.readLine();
			value=l.split(",");
			for(String val:value)
			{
				ind=Integer.parseInt(val.trim());
				try {
					br = new BufferedReader(new FileReader(cfile+val));
				}
				catch (FileNotFoundException e) { arr.add(ind);}
			}
		}catch (FileNotFoundException e) { } catch (IOException e) {}
	}
	for(int i=freeEnd+1;i<maxBlocks;i++)
	{
		try {
			br = new BufferedReader(new FileReader(cfile+""+i));
		}
			catch (FileNotFoundException e) {
				if(!(arr.contains(i)))
					{System.out.println("fusedata."+""+i+" does not exist in the free block list");
					 addFreeBlock(i);
					}
			} 
	}
	checkDirInFreeBlock();
}
public void addFreeBlock(int ind)
{
	int checkWrite=0, exist=0;
	for(int j=freeStart;j<=freeEnd;j++)
	{
		try{
		br = new BufferedReader(new FileReader(cfile+""+j));
		l = br.readLine();
		value=l.split(",");
		for(String val:value)
		{
			if(Integer.parseInt(val.trim())==ind)
			{
				System.out.println("Index to be added already exists");
				exist=1;
				break;
			}
		}
		}catch (FileNotFoundException e) { } catch (IOException e) {}
	}
	if(exist==0)
	{
	for(int i=freeStart;i<=freeEnd;i++)
	{
		try {
			if(ind>=maxBlocks||ind<=freeEnd||(ind>0&&ind<freeStart))
			{
				System.out.println("Index exceeds the maxBlocks size or is less than or equal to freeEnd");
				break;
			}
			if(arrd.contains(ind))
			{
				System.out.println("Directory cannot be added to free block list");
			}
			br = new BufferedReader(new FileReader(cfile+""+i));
			l = br.readLine();
			value=l.split(",");
			if(value.length==400)
				continue;
			else
			{
				checkWrite=1;
				String str = "";
				for(String s:value)
				{
					str=str+s.trim()+", ";
				}
				str=str+""+ind;
				PrintWriter p=new PrintWriter(cfile+""+i);
				p.print(str);
				p.close();
				break;
			}
		}catch (FileNotFoundException e) { } catch (IOException e) {}
	}
	if(checkWrite==0)
		System.out.println("Wrong index or No Space to add Free Block so not added ");
	}
}
public void checkDirInFreeBlock()
{
	int writeIt=0;
	for(int i=freeStart;i<=freeEnd;i++)
	{
		try {
			br = new BufferedReader(new FileReader(cfile+""+i));
			l = br.readLine();
			value=l.split(",");
			String str="";
			int blk = 0;
			for(String val:value)
			{
				if(!(arrd.contains(Integer.parseInt(val.trim())))&&!(arrf.contains(Integer.parseInt(val.trim())))&&!(arrs.contains(Integer.parseInt(val.trim()))))
				{
					str=str+val.trim()+", ";
				}
				else
				{
					writeIt=1;
					blk=Integer.parseInt(val.trim());
				}
			}
			if(writeIt==1)
			{
				PrintWriter p=new PrintWriter(cfile+""+i);
				p.print(str);
				p.close();
				System.out.println("Block "+blk+" removed");
				writeIt=0;
			}
		}catch (FileNotFoundException e) { } catch (IOException e) {}
	}
}
public void chkCurrentParent()
{
	int chkcurr=0, chkpar=0, writeIt=0;
	int parent = 0;
	for(int j:arrd)
	{
		String str1 = "";
		if(arrd.indexOf(j)==0)
			parent=j;
		else
			parent=arrd.get(arrd.indexOf(j)-1);
		try {
			br = new BufferedReader(new FileReader(cfile+j));
			l = br.readLine();
			value=l.split(",");
			for(String str:value)
			{
				if(str.contains("d:.:"))
				{
					chkcurr=1;
					System.out.println("Current directory . exists");
					if(str.contains("}"))
					{
						if(Integer.parseInt(str.substring(str.indexOf("d:.:")+4,str.indexOf("}")))==j)
						{
							System.out.println("Current directory . is correct");
							str1=str1+str;
						}
						else
						{
							System.out.println("Current directory . is incorrect");
							writeIt=1;
							str1=str1+str.replace(str.substring(str.indexOf("d:.:")+4,str.indexOf("}")), j+"");
						}
					}
					else
					{
					if(Integer.parseInt(str.substring(str.indexOf("d:.:")+4))==j)
					{
						System.out.println("Current directory . is correct");
						str1=str1+str.trim()+",";
					}
					else
					{
						System.out.println("Current directory . is incorrect");
						writeIt=1;
						str1=str1+str.replace(str.substring(str.indexOf("d:.:")+4),j+"")+",";
					}
					}
				}
				else if(str.contains("d:..:"))
				{
					chkpar=1;
					System.out.println("Parent directory .. exists");
					if(str.contains("}"))
					{
						if(Integer.parseInt(str.substring(str.indexOf("d:..:")+5,str.indexOf("}}")))==parent)
						{
							System.out.println("Parent directory .. is correct");
							str1=str1+str;
						}
						else
						{
							System.out.println("Parent directory .. is incorrect");
							writeIt=1;
							str1=str1+str.replace(str.substring(str.indexOf("d:..:")+5,str.indexOf("}")), parent+"");
						}
					}
					else
					{
					if(Integer.parseInt(str.substring(str.indexOf("d:..:")+5))==parent)
					{
						System.out.println("Parent directory .. is correct");
						str1=str1+str.trim()+",";
					}
					else
					{
						System.out.println("Parent directory1 .. is incorrect");
						writeIt=1;
						str1=str1+str.replace(str.substring(str.indexOf("d:..:")+5),parent+"")+",";
					}
					}
				}
				else
				{
					if(str.contains("}"))
						str1=str1+str;
					else
						str1=str1+str+",";
				}
			}
			if(chkcurr==0)
			{
				System.out.println("Current directory . does not exist");
				if(str1.contains("{}"))
					str1=str1.substring(0,str1.indexOf("}"))+"d:.:"+j+str1.substring(str1.indexOf("}"));
				else
				    str1=str1.substring(0,str1.indexOf("}"))+", "+"d:.:"+j+str1.substring(str1.indexOf("}"));
				writeIt=1;
			}
			if(chkpar==0)
			{
				System.out.println("Parent directory .. does not exist");
				str1=str1.substring(0,str1.indexOf("}"))+", "+"d:..:"+parent+str1.substring(str1.indexOf("}"));
				writeIt=1;
			}
			if(writeIt==1)
			{
				PrintWriter p=new PrintWriter(cfile+""+j);
				p.print(str1);
				p.close();
				writeIt=0;
			}
		}catch (FileNotFoundException e) {} catch (IOException e) {} 
	}
}
public void countLinks()
{
	for(int j:arrd)
	{
		try {
			br = new BufferedReader(new FileReader(cfile+j+""));
			l = br.readLine();
			value=l.split(",");
			String str="";
			int linkCount=Integer.parseInt(value[7].substring(value[7].indexOf(":")+1));
			int ind=8;
			int count=0;
			while(!value[ind].contains("}"))
			{
				count++;
				ind++;
			}
			count++;
			if(count==linkCount)
				System.out.println("LinkCount matches the number of links in the filename_to_inode_dict");
			else
			{
				System.out.println("LinkCount is incorrect");
				value[7]=value[7].replace(linkCount+"", count+"");
				for(int i=0;i<value.length;i++)
				{
					if(value[i].contains("}"))
					    str=str+value[i];
					else
						str=str+value[i]+",";
				}
				PrintWriter p=new PrintWriter(cfile+""+j);
				p.print(str);
				p.close();
			}
		}catch (FileNotFoundException e) {} catch (IOException e) {} 
	}
}
public void chkLocationPointer()
{
	String value1[]=null;
	int writeIt=0;
	String str="";
	for(int j:arrf)
	{
		try {
			br = new BufferedReader(new FileReader(cfile+j+""));
			l = br.readLine();
			value=l.split(",");
			String locP=value[8].substring(value[8].indexOf("location:")+9,value[8].indexOf("}")).trim();
			String indir=value[8].substring(value[8].indexOf("indirect:")+9,value[8].indexOf("location:")).trim();
			FileReader f=new FileReader(cfile+locP);
			br = new BufferedReader(f);
			l = br.readLine();
			value1=l.split(",");
			if(value1.length>1)
			{
				if(!indir.equals("1"))
				{
					System.out.println("indirect is incorrect");
					value[8]=value[8].replace(value[8].substring(value[8].indexOf("indirect:"),value[8].indexOf("location:")).trim(), "indirect:1");
					writeIt=1;
				}
				else
					System.out.println("Location pointer and indirect are correct");
			}
			else
			{
				if(!indir.equals("0"))
				{
					System.out.println("indirect is incorrect");
					value[8]=value[8].replace(value[8].substring(value[8].indexOf("indirect:"),value[8].indexOf("location:")).trim(), "indirect:0");
					writeIt=1;
				}
				else
					System.out.println("Location pointer and indirect are correct");
			}
			f.close();
			if(writeIt==1)
			{
				for(String val:value)
				{
					if(val.contains("}"))
						str=str+val;
					else
					    str=str+val+",";
				}
				PrintWriter p=new PrintWriter(cfile+""+j);
				p.print(str);
				p.close();
			}
		}catch (FileNotFoundException e) {} catch (IOException e) {} 
	}
}
public void chkSizeLocP()
{
	String value1[]=null;
	String str="";
	String locP = null;
	int change=0;
	for(int j:arrf)
	{
		try {
			br = new BufferedReader(new FileReader(cfile+j+""));
			l = br.readLine();
			value=l.split(",");
			int size=Integer.parseInt(value[0].substring(value[0].indexOf(":")+1));
			int indirect=Integer.parseInt(value[8].substring(value[8].indexOf("indirect:")+9,value[8].indexOf("location:")).trim());
			locP=value[8].substring(value[8].indexOf("location:")+9,value[8].indexOf("}")).trim();
			if(size>0)
			{
				if(size<blocksize)
				{
					FileReader f=new FileReader(cfile+locP);
					br = new BufferedReader(f);
					l = br.readLine();
					value1=l.split(",");
					if(indirect!=0)
						value[8]=value[8].replace(value[8].substring(value[8].indexOf("indirect:"),value[8].indexOf("location:")).trim(), "indirect:0");
					Path path=Paths.get(cfile+value1[0].trim());
					value[8]=value[8].replace(value[8].substring(value[8].indexOf("location:"),value[8].indexOf("}")).trim(), "location:"+value1[0].trim());
				    for(String val:value)
				    {
				    	if(val.contains("}"))
				    		str=str+val;
				    	else
				    		str=str+val+",";
				    	PrintWriter p=new PrintWriter(cfile+""+j);
				    	p.print(str);
				    	p.close();
				    }
				    if(Files.exists(path))
				    {
				    	f.close();
				    	path=Paths.get(cfile+locP.trim());
				    	Files.deleteIfExists(path);
				    	System.out.println("File "+cfile+locP.trim()+" deleted");
				    	addFreeBlock(Integer.parseInt(locP.trim()));
				    }
				}
				else if(size>blocksize)
				{
					locP=value[8].substring(value[8].indexOf("location:")+9,value[8].indexOf("}")).trim();
					br = new BufferedReader(new FileReader(cfile+locP));
					l = br.readLine();
					value1=l.split(",");
					if(indirect!=1){ change=1;
						value[8]=value[8].replace(value[8].substring(value[8].indexOf("indirect:"),value[8].indexOf("location:")).trim(), "indirect:1");}
					double ceilV=Math.ceil(size/blocksize);
					if(size<(blocksize*value1.length))
					{
						System.out.println("Location array sufficient to store file");
						if(ceilV!=(double)value1.length)
						{
							int i;
							for(i=0;i<(int) ((ceilV)+1);i++)
							{
								if(i<(int) ((ceilV)+1))
								{
									if((i+1)==(int) ((ceilV)+1))
										str=str+value1[i];
									else
										str=str+value1[i]+",";
								}
							}
							PrintWriter p=new PrintWriter(cfile+""+locP);
						    p.print(str);
						    p.close();
						    while(i<value1.length)
						    {
						    	if(arrd.contains(Integer.parseInt(value1[i].trim()))){
						    		i++;
						    	}
						    	else
						    	{
						    		System.out.println(i);
							    	Path path=Paths.get(cfile+value1[i]);
								    Files.deleteIfExists(path);
								    System.out.println("File "+cfile+value1[i].trim()+" deleted");
								    addFreeBlock(Integer.parseInt(value1[i].trim()));
								    i++;
						    	}
						    		
						    }
						}
						if(change==1)
						{
							for(String val:value)
						    {
						    	if(val.contains("}"))
						    		str=str+val;
						    	else
						    		str=str+val+",";
						    	PrintWriter p=new PrintWriter(cfile+""+j);
						    	p.print(str);
						    	p.close();
						    }
						}
					}
					else if(size>(blocksize*(value1.length-1)))
						System.out.println("Location array insufficient to store file");
					}
			}
		}catch (FileNotFoundException e) {System.out.println("Location points to a text file");} catch (IOException e) {} 
	}
}
public static void main(String[] args) {
	Csefsck cs=new Csefsck();
	cs.readSuperBlock();
	cs.devIdcheck();
	cs.checkTime();
	cs.freeBlockCheck();
	cs.chkCurrentParent();
	cs.countLinks();
	cs.chkLocationPointer();
	cs.chkSizeLocP();
}
}