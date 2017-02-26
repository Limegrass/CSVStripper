import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Strips extraneous data for two different data collection rates.
 * Put the two data files in the same folder as this .java file
 * In Windows (on a personal computer with Java installed)
 * Press the Windows+R, type cmd then enter.
 * Navigate to the folder with this java file by cd (Directory path)
 * Type javac CSVStripper.java in the directory, then java CSVStripper (no file extensions)
 * The output should be in the same folder.
 * Inputs must have the time in the first column of the CSV File.
 * @author James Ni
 */
public class CSVStripper 
{
	//Maximum difference to take a data point for
	public static final double EPSILON = 5E-2;	
	//Separation value (comma for a comma-separated-values file)
	public static final String SEPARATOR = ","; 
	//File name for the slower data collection rate, slowRateInput in our case
	public static final String SLOWDATA = "Run2Flir.csv";
	//File name for the faster data collection rate, fastRateInput in our case
	public static final String FASTDATA = "Run2Pasco.csv";
	//File name for the output file
	public static final String OUTPUTNAME = "Matched Data.csv";
	
	public static void main(String[] args)
	{
		//Things to read input and write out.
		//Initialized in the try
		BufferedReader slowRateInput, fastRateInput;
		BufferedWriter output;

		try{
			//Initialize all inputs and outputs
			slowRateInput = new BufferedReader(new FileReader(SLOWDATA));
			fastRateInput = new BufferedReader(new FileReader(FASTDATA)); 
			File outputFile = new File(OUTPUTNAME);
			output = new BufferedWriter(new FileWriter(outputFile));

			//Strings to hold the lines of input
			String slowLine = "";	
			String fastLine = "";
			
			//Header to add, if desired
			output.write("PASCO,,FLIR\n");

			//Reads a line first, so it can keep the line between data inputs of the slower rate
			fastLine = fastRateInput.readLine();
			String[] fastDataPoint = fastLine.split(SEPARATOR);
			
			while((slowLine = slowRateInput.readLine())!=null)
			{
				//Used the polled data line and split it up into the time step and not.
				String[] slowDataPoint = slowLine.split(SEPARATOR);
				//Parse the time step
				double slowTime = Double.parseDouble(slowDataPoint[0]);
				double fastTime = Double.parseDouble(fastDataPoint[0]);
				//For if it reaches the end of line on the faster time step.
				boolean noFastLine = false;
				//Reads values in until the different is below the Epsilon, or you passed the slower time step time.
				while(fastTime < slowTime && slowTime-fastTime>EPSILON)
				{
					if((fastLine = fastRateInput.readLine())!=null)
					{
						fastDataPoint = fastLine.split(SEPARATOR);
						fastTime = Double.parseDouble(fastDataPoint[0]);
					}
					else
					{
						noFastLine = true;
						break;
					}
				}
				//End the program if reaches the end of the faster data collection file.
				if(noFastLine)
				{
					output.close();
					fastRateInput.close();
					slowRateInput.close();
					break;
				}
				if(slowTime-fastTime<EPSILON)
				{
					//Test the next time step for a better fit before choosing a data point. The next step is saved.
					//Writes the better fit into the data file.
					String testNext;
					if((testNext = fastRateInput.readLine())!=null)
					{
						String[] nextDataPoint = testNext.split(SEPARATOR);
						double nextTime = Double.parseDouble(nextDataPoint[0]);
						if(Math.abs(slowTime-nextTime)<slowTime-fastTime)
						{
							System.out.print(fastLine);
							System.out.println("," + slowLine);
							output.write(testNext + "," + slowLine + '\n');
						}
						else
						{
							System.out.print(fastLine);
							System.out.println("," + slowLine);
							output.write(fastLine + "," + slowLine + '\n');
						}
					}
				}
			}
		} 
		catch(IOException err)
		{
			err.printStackTrace();
		}

	}
}
