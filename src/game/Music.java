package game;

import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

//음악을 관리하는 클래스
//음악 설정, 음악 변경,등
public class Music {
	private String path; //음악파일의 경로를 저장받음
	private Clip clip; //클립을 필드로 생성
	
	public Music(String path) {
		this.path = path;
	}
	
	//음악 실행
	public void loadAndStartMusic() {
		loadAudio(path);
		startMusic();
	}
	
	//음악 변경
	public void changeMusic(String path) {
		this.path = path; //음악 경로 변
		loadAudio(path);
		startMusic();
	}
	
	//로드되어있는 오디오를 실행시킨다.
	public void startMusic() {
		clip.start();
	}
	
	
	public void loadAudio(String pathName) {
		try{
    		clip = AudioSystem.getClip();
    		File audioFile = new File(pathName);
    		AudioInputStream ais = AudioSystem.getAudioInputStream(audioFile);
            clip.open(ais);
            //프로그램을 종료하기전까지 반복재생함
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }catch (Exception ex){
        	System.out.println("불러오기 오류");
	    }
	}
	
	
	//중지된 이후부터 음악을 시작
	public void resumeMusic() {
		clip.start();
	}
	
	public void musicReStart() {
		clip.setFramePosition(0); //재생 위치를 처음으로 옮김
		clip.start(); //처음부터 다시 시작
	}
	
	//음악 종료
	public void musicStop() {
		clip.stop(); //오디오 재생 중단
	}
}
