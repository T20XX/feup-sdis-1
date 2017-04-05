package server.protocol;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import utils.*;
import server.main.Peer;
import server.task.initiatorPeer.PutChunk;

public class Backup {

	public Backup(String filePath, int replicationDeg) {

		File f = new File(filePath);
		long fileLength = f.length();
		byte[] chunk = new byte[64000];

		try {
			InputStream in = new FileInputStream(f);

			//Number of Chunks needed for the file
			int numChunks = (int)(fileLength/64000)+1;
			//Size of the last chunk
			int lastChunkSize = (int)(fileLength%64000);
			//get File ID
			String fileID = Utils.getFileID(f.getPath());

			//Create chunks and call PutChunks threads
			for(int i = 0;i < numChunks;i++){
				int numbytesRead;
				//Special case 'Last Chunk'
				if(i == numChunks-1){
					chunk = new byte[lastChunkSize];
					//Only reads if the size of the last Chunk is not 0, special case from multiples of 64000 in the file sizes
					if(lastChunkSize != 0){
						numbytesRead = in.read(chunk, 0, lastChunkSize);
					}
				}else numbytesRead = in.read(chunk, 0, 64000);

				Thread putChunkThread = new Thread(new PutChunk(
						Peer.serverID,
						fileID,
					    i,
					    replicationDeg,
					    chunk
					    ));
				putChunkThread.start();
				try {
					putChunkThread.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			Utils.writeMD(filePath, f.lastModified(), fileID);

			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
