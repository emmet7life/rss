package yay.poloure.simplerss;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;

public class parsered
{
	public String file;
	
	public parsered(String file_path)
	{
		this.file = file_path;
		parse_local_xml(file_path);
	}
	
	private void parse_local_xml(String file_name)
	{
		try
		{
			final String[] start = new String[]{"<name>", "<id>", "<link>",  "<description>", "<title>", "<content type=\"html\">", "<content>", "<icon>"};
			final String[] end = new String[]{"</name>", "</id>", "</link>", "</description>", "</title>", "</content>", "</content>", "</icon>"};
			String[] of_types = new String[]{"<name>", "<id>", "<link>",  "<description>", "<title>", "<content type=\"html\">", "<content>", "<icon>",
				"</name>", "</id>", "</link>", "</description>", "</title>", "</content>", "</content>", "</icon>", "<entry", "<item", "</entry", "</item"}; 
				
			File in = new File(file_name);
			BufferedReader reader = new BufferedReader(new FileReader(in));
			int filesize = (int) (in.length() + 1);
			char[] current = new char[1];
			char[] buf = new char[filesize + 1];
			String end_tag = "";
			reader.mark(2);

			while(reader.read() != -1)
			{
				reader.reset();
				String buf_string = get_next_tag(reader, of_types, file_name);
				if((buf_string.contains("<entry"))||(buf_string.contains("<item")))
					to_file(file_name + ".content.txt", "\n", true);
				else if((buf_string.contains("</entry"))||(buf_string.contains("</item")))
					check_for_image(file_name);
				else
				{
					for(int i=0; i<start.length; i++)
					{
						if(buf_string.equals(start[i]))
						{
							if(buf_string.equals("<content type=\"html\">"))
								buf_string = "<description>";
							else if(buf_string.equals("<content>"))
								buf_string = "<description>";
							to_file(file_name + ".content.txt", buf_string.substring(1, buf_string.length() - 1) + "|", true);
							while(!(end_tag.equals(end[i])))
							{
								int count = 0;
								current = new char[1];
								buf = new char[filesize];
								while(current[0] != '<'){
									buf[count] = current[0];
									reader.read(current, 0, 1);
									count++;
								}
								String cont = (new String(buf)).trim()
									.replace("\r"," ")
									.replace("\n","")
									.replace("&lt;", "<")
									.replace("&gt;", ">");
									
								if(cont.contains("img src="))
									to_file(file_name + ".content.dump.txt", cont.substring(cont.indexOf("src=\"") + 5, cont.indexOf("\"", cont.indexOf("src=\"") + 6)) + "\n", false);

									cont = cont.replaceAll("\\<.*?\\>", "")
									.replace("&quot;", "\"")
									.replace("&amp;", "&")
									.replace("mdash;", "—")
									.trim();
								to_file(file_name + ".content.txt", cont, true);
								
								buf = new char[1024];
								count = 0;
								while(current[0] != '>')
								{
									buf[count] = current[0];
									reader.read(current, 0, 1);
									count++;
								}
								buf[count] = current[0];
								end_tag = new String(buf);
								end_tag = end_tag.trim();
								if(!(end_tag.equals(end[i])))
									to_file(file_name + ".content.txt", end_tag, true);
							}
							to_file(file_name + ".content.txt", "|", true);
							break;
						}
					}
				}
				reader.mark(2);
			}
		}
		catch(Exception e){
		}
	}

	private void check_for_image(String file_name)
	{
		File im = new File(file_name + ".content.dump.txt");
		try
		{
			BufferedReader image = new BufferedReader(new FileReader(im));
			String image_url = image.readLine();
			if(image_url.length()>6)
				to_file(file_name + ".content.txt" , "image|" + image_url + "|", true);
		}
		catch(Exception e){
		}
		im.delete();
	}

	private String get_next_tag(BufferedReader reader, String[] types, String file_name) throws Exception
	{
		boolean found = false;
		String tag = "";
		int eof = 0;
		while((!found)&&(eof != -1)){
			int count = 0;
			char[] current = new char[1];
			char[] buffer = new char[1024];
			while((current[0] != '<')&&(eof != -1))
				eof = reader.read(current, 0, 1);
				
			while((current[0] != '>')&&(eof != -1))
			{
				buffer[count] = current[0];
				reader.read(current, 0, 1);
				count++;
			}
			buffer[count] = current[0];
			tag = (new String(buffer)).trim();
			
			if(tag.contains("img src="))
				to_file(file_name + ".content.dump.txt", tag.substring(tag.indexOf("src=\"") + 5, tag.indexOf("\"", tag.indexOf("src=\"") + 6)) + "\n", false);

			for(int i=0; i<types.length; i++)
			{
				if(tag.contains(types[i]))
					found = true;
			}
		}
		if(eof == -1)
			return "eof";
		else
			return tag;
	}

	private void to_file(String file_namer, String string, boolean append)
	{
		try{
			BufferedWriter out = new BufferedWriter(new FileWriter(file_namer, append));
			out.write(string);
			out.close();
		}
		catch(Exception e){}
	}
}
