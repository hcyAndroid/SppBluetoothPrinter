package com.issyzone.classicblulib.tools;



public class BitmapUtils {


//    public  void test(){
//        Imgproc.resize();
//    }
    public static String[] photoZoomToVideo(String inputPath, int position, String outputPath) {
        String zoomCmd = "ffmpeg -i -vf ";
        switch (position) {
            case 1:
                zoomCmd += "zoompan='1.3':x='if(lte(on,1),(iw/zoom)/2,x-0.5)':y='if(lte(on,1),(ih-ih/zoom)/2,y)':d=180";
                break;
            case 2:
                zoomCmd += "zoompan='1.3':x='if(lte(on,-1),(iw-iw/zoom)/2,x+0.5)':y='if(lte(on,1),(ih-ih/zoom)/2,y)':d=180";
                break;
            case 3:
                zoomCmd += "zoompan='1.3':x='if(lte(on,1),(iw-iw/zoom)/2,x)':y='if(lte(on,-1),(ih-ih/zoom)/2,y+0.5)':d=180";
                break;
            case 4:
                zoomCmd += "zoompan='1.3':x='if(lte(on,1),(iw-iw/zoom)/2,x)':y='if(lte(on,1),(ih/zoom)/2,y-0.5)':d=180";
                break;
            case 0:
            default:
                zoomCmd += "zoompan=z='min(zoom+0.0015,1.5)':x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':d=180";
                break;
        }
        return insert(zoomCmd.split(" "), 2, inputPath, outputPath);
    }

    /**
     * insert inputPath and outputPath into target array
     */
    private static String[] insert(String[] cmd, int position, String inputPath, String outputPath) {
        if (cmd == null || inputPath == null || position < 2) {
            return cmd;
        }
        int len = (outputPath != null ? (cmd.length + 2) : (cmd.length + 1));
        String[] result = new String[len];
        System.arraycopy(cmd, 0, result, 0, position);
        result[position] = inputPath;
        System.arraycopy(cmd, position, result, position + 1, cmd.length - position);
        if (outputPath != null) {
            result[result.length - 1] = outputPath;
        }
        return result;
    }
}
