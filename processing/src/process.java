
import java.io.*;
import org.json.simple.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.FileOutputStream;
public class process
{

	// Parties are very hard coded atm. However I'm not sure if these parties are set for an acceptable amount of time
	// and I cba to implement some key-value bs to find out all the parties in the voting if it's gonna be these 8 most
	// of the time anyway!
	//S, M, SD, MP, C, V, L, KD
	private static int numParties = 8;
	private static int S = 0;
	private static int M =	1;
	private static int SD =	2;
	private static int MP =	3;
	private static int C =	4;
	private static int V =	5;
	private static int L =	6;
	private static int KD =	7;
	private static String[] parties = {"S", "M", "SD", "MP", "C", "V", "L", "KD"};
	private static int Ja = 0;
	private static int Avstar = 1;
	private static int Nej = 2;

	public static void main(String[] args)
	{
		JSONParser parser = new JSONParser();
		File folder = new File("input");
		File[] listOfFiles = folder.listFiles();

		JSONArray outputArray = new JSONArray();

		for (File jsonFile : listOfFiles)
		{
			if (jsonFile.isFile())
			{
				String fileName = jsonFile.getName();

				System.out.println(fileName);

				String[] extension = fileName.split("\\.");
				if(!extension[extension.length-1].equals("json"))
				{
					System.out.println("not Json");
					continue;
				}

				int[][] voteList = new int[numParties][3];
				JSONObject jsonObject = null;

				try
				{
					InputStreamReader temp = new InputStreamReader(new FileInputStream(jsonFile),"UTF-8");
					temp.skip(1);
					jsonObject =  (JSONObject) parser.parse(temp);
					JSONArray votering = (JSONArray) ((JSONObject)jsonObject.get("dokvotering")).get("votering");


					for (Object o : votering) {
						JSONObject vote = (JSONObject) o;
						String parti = (String) vote.get("parti");
						String rost = (String) vote.get("rost");
						String firstRost = rost.substring(0,1);					//Shit fucks up if you use å in Avstår for equals
						int rostIndex;
						switch (firstRost)
						{
							case "J":
								rostIndex = Ja;
								break;
							case "F":
							case "A":
								rostIndex = Avstar;
								break;
							case "N":
								rostIndex = Nej;
								break;
							default:
								System.out.println(rost);
								rostIndex = 40;	//Will cause index out of bounds error
						}
						switch (parti)
						{
							case "S":
								voteList[S][rostIndex]++;
								break;
							case "M":
								voteList[M][rostIndex]++;
								break;
							case "SD":
								voteList[SD][rostIndex]++;
								break;
							case "MP":
								voteList[MP][rostIndex]++;
								break;
							case "FP":		//Folkpartiet, old name for liberalerna
							case "L":
								voteList[L][rostIndex]++;
								break;
							case "C":
								voteList[C][rostIndex]++;
								break;
							case "KD":
								voteList[KD][rostIndex]++;
								break;
							case "V":
								voteList[V][rostIndex]++;
								break;
							case "-":
								break;		//someone who isn't in a party????
							default:
								System.out.println(parti);
								voteList[10000][10000]++; //(hopefully) throws index out of bounds error. A party not represented in the model participated in the vote.
						}
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
				} catch (ParseException e)
				{
					e.printStackTrace();
				}
				
				
				String[] parts = fileName.split("-");
				String documentCode = parts[0];
				
				//Can be dealt with prettier when looking at the JSON code...
				/*int point;
				boolean isPoint = true;
				try
				{
					point = Integer.parseInt(parts[1]);
					if(point > 100)
						isPoint = false;
				}
				catch (NumberFormatException e)
				{
					isPoint = false;
				}*/

				JSONObject arrayEntry = new JSONObject();
				arrayEntry.put("DocumentCode",  documentCode);
				arrayEntry.put("Point", parts[1]);
				JSONObject votes = new JSONObject();
				for(int i = 0; i < parties.length; i++)
				{
					votes.put(parties[i], voteList[i][Ja]+"-"+voteList[i][Avstar]+"-"+voteList[i][Nej]);
				}
				arrayEntry.put("Votes", votes);

				outputArray.add(arrayEntry);
			}
		}

		JSONObject output = new JSONObject();
		output.put("Votering", outputArray);
		try
		{
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("output.json"), "UTF-8"));
			bw.write(output.toJSONString());
			bw.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}

	}
}