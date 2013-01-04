import static javax.media.opengl.GL.GL_LINES;
import static javax.media.opengl.GL.GL_LINE_LOOP;
import static javax.media.opengl.GL2.GL_RENDER;
import static javax.media.opengl.GL2.GL_SELECT;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;

import gui.GUI;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.media.nativewindow.util.Point;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;

import objects.Point3f;

import scene.FPSCounter;
import scene.Scene;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.newt.event.awt.AWTKeyAdapter;
import com.jogamp.newt.event.awt.AWTMouseAdapter;

@SuppressWarnings("serial")
public class Renderer extends GLCanvas implements GLEventListener {
	GL2 gl;
	GLU glu;
	GLUT glut;

	Mouse mouse = new Mouse();
	Keyboard keyboard = new Keyboard();
	Interpreter interpreter = new Interpreter();
	FPSCounter fps = new FPSCounter();
	String consoleInput = new String();
	Integer lastObject = null;
	Point3f click = new Point3f(0, 0, 0);

	private static final int BUFFER_SIZE = 512;
	// LIGHT
	FloatBuffer whiteMaterial, blackMaterial;
	FloatBuffer light_position;

	// Camera
	float cameraDistance = 10;
	float zoomStep = 0.1f;
	float cameraAngleInXZ = 0;
	float cameraAngleInYZ = 0;
	float cameraAngleInYX = 0;
	float panStep = 0.01f;

	// GUI
	GUI gui;

	// Scene
	Scene scene;

	public Renderer() {
		this.addGLEventListener(this);
		AWTMouseAdapter mouseAdapter = new AWTMouseAdapter(mouse);
		this.addMouseListener(mouseAdapter);
		this.addMouseMotionListener(mouseAdapter);

		AWTKeyAdapter keyAdapter = new AWTKeyAdapter(keyboard);
		this.addKeyListener(keyAdapter);
	}

	public void init(GLAutoDrawable drawable) {
		gl = drawable.getGL().getGL2();
		glu = new GLU();
		glut = new GLUT();
		scene = new Scene(gl, glut);
		gui = new GUI(gl, glut);

		whiteMaterial = make(new float[] { 1.0f, 1.0f, 1.0f, 1.0f });
		blackMaterial = make(new float[] { 0.0f, 0.0f, 0.0f, 1.0f });
		FloatBuffer mat_shininess = make(new float[] { 100.0f });

		FloatBuffer lmodel_ambient = make(new float[] { 0.2f, 0.2f, 0.2f, 1.0f });
		gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, lmodel_ambient);

		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glShadeModel(GL2.GL_SMOOTH);
		// how to reflect specular light
		gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR, whiteMaterial);
		gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SHININESS, mat_shininess);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, whiteMaterial);
		// light is positional
		light_position = make(new float[] { 20.0f, 2.0f, 0.0f, 1.0f });
		FloatBuffer light_diffuse = make(new float[] { 1.0f, 0.0f, 0.0f, 1.0f });
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, light_diffuse);

		FloatBuffer light_specular = make(new float[] { 0.0f, 0.0f, 1.0f, 1.0f });
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, light_specular);

		gl.glClearDepth(1.0f);
		gl.glDepthFunc(GL.GL_LEQUAL);
		gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);

	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {

		if (height <= 0) {
			height = 1;
		}
		final float h = (float) width / (float) height;
		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(45.0f, h, 0.1f, 100.0f);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	public void display(GLAutoDrawable drawable) {
		fps.startCounter();
		interpreter.consoleInput = keyboard.message;
		interpreter.manager = scene.manager;
		processCommands();

		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, light_position);
		gl.glLoadIdentity();

		// glut.glutSolidSphere(1.0, 20, 20);
		// gl.glMatrixMode(GL2.GL_MODELVIEW);
		// gl.glLoadIdentity();

		// gl.glPushMatrix();
		// gl.glLoadIdentity();
		// gl.glRotatef(10, 0, 1, 0);

		setCamera();
		setText("test1", 1, 1);
		gl.glEnable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_LIGHT0);
		gl.glEnable(GL2.GL_DEPTH_TEST);

		scene.make(whiteMaterial, blackMaterial);

		if (keyboard.showViewVolume)
			showViewVolume(-10.0f, 10.0f, -10.0f, 10.0f, -10.0f, 10.0f);

		gl.glDisable(GL2.GL_DEPTH_TEST);
		gl.glDisable(GL2.GL_LIGHT0);
		gl.glDisable(GL2.GL_LIGHTING);

		// gl.glPopMatrix();
		// gl.glLoadIdentity();

		gui.cameraDistance = cameraDistance;
		gui.cameraAngleInYZ = cameraAngleInYZ;
		gui.cameraAngleInXZ = cameraAngleInXZ;

		gui.enable2D();

		if (keyboard.window == 0) {
			gui.showDefault(scene.manager.objects.size(), null, scene.selected);
			gui.showFPS(fps.rate);
		}
		if (keyboard.window == 1)
			gui.showConsole(interpreter.processInput());

		if (keyboard.window == 2)
			gui.showHelp();

		gui.disable2D();
		// if (mouse.point != null) {
		// userPickAt(mouse.point);
		// click = getMouseClickLocation(mouse.point);
		// mouse.point = null;
		// }
		keyboard.message = interpreter.consoleInput;
		scene.manager = interpreter.manager;
		// Mouse target
		// scene.manager.addSphere(location, color);

		// gl.glFlush();
		fps.postCounter();

	}

	private void setText(String string, float x, float y) {
		// gl.glLoadIdentity();
		gl.glRasterPos3f(x, y, 0);
		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, string);
	}

	private void setCamera() {
		Point3f eye = new Point3f(0.f, 0.f, 0.f);
		eye.x = (float) (cameraDistance * Math.cos(cameraAngleInXZ));
		eye.y = (float) (cameraDistance * Math.sin(cameraAngleInYZ));
		eye.z = (float) (cameraDistance * Math.sin(cameraAngleInXZ));
		;

		float centerX = 0;
		float centerY = 0;
		float centerZ = 0;
		float upX = 0.f;
		float upY = 1.f;
		float upZ = 0.f;

		gl.glMatrixMode(GL_MODELVIEW);
		gl.glLoadIdentity();

		glu.gluLookAt(eye.x, eye.y, eye.z, centerX, centerY, centerZ, upX, upY,
				upZ);
	}

	void processCommands() {
		if (keyboard.resetView) {
			cameraDistance = 128;
			cameraAngleInXZ = 0;
			cameraAngleInYZ = 0;
			cameraAngleInYX = 0;
			keyboard.resetView = false;
		}
		if (keyboard.zoomOut) {
			cameraDistance += zoomStep;
		}
		if (keyboard.zoomIn) {
			cameraDistance -= zoomStep;
		}
		if (keyboard.panLeft) {
			cameraAngleInXZ -= panStep;
		}
		if (keyboard.panRight) {
			cameraAngleInXZ += panStep;
		}
		if (keyboard.panUp) {
			cameraAngleInYZ -= panStep;
			cameraAngleInYX -= panStep;
		}
		if (keyboard.panDown) {
			cameraAngleInYZ += panStep;
			cameraAngleInYX += panStep;
		}

		if (keyboard.clearSelection) {
			scene.selected.clear();
			keyboard.clearSelection = false;
		}
		if (keyboard.deleteSelected) {
			for (Integer hash : scene.selected) {
				scene.manager.removeObject(hash);
			}
			scene.selected.clear();
			keyboard.deleteSelected = false;
		}
		if (keyboard.resetScene) {
			scene.manager.objects.clear();
			keyboard.resetScene = false;
		}
		if (keyboard.loadScene) {
			scene.manager.loadMap();
			keyboard.loadScene = false;
		}
	}

	public void showViewVolume(float x1, float x2, float y1, float y2,
			float z1, float z2) {
		// gl.glTranslatef(20f,0,0);
		gl.glColor3f(1.0f, 1.0f, 1.0f);
		gl.glBegin(GL_LINE_LOOP);
		gl.glVertex3f(x1, y1, -z1);
		gl.glVertex3f(x2, y1, -z1);
		gl.glVertex3f(x2, y2, -z1);
		gl.glVertex3f(x1, y2, -z1);
		gl.glEnd();

		gl.glBegin(GL_LINE_LOOP);
		gl.glVertex3f(x1, y1, -z2);
		gl.glVertex3f(x2, y1, -z2);
		gl.glVertex3f(x2, y2, -z2);
		gl.glVertex3f(x1, y2, -z2);
		gl.glEnd();

		gl.glBegin(GL_LINES);
		gl.glVertex3f(x1, y1, -z1);
		gl.glVertex3f(x1, y1, -z2);
		gl.glVertex3f(x1, y2, -z1);
		gl.glVertex3f(x1, y2, -z2);
		gl.glVertex3f(x2, y1, -z1);
		gl.glVertex3f(x2, y1, -z2);
		gl.glVertex3f(x2, y2, -z1);
		gl.glVertex3f(x2, y2, -z2);
		gl.glEnd();
	}

	private Point3f getMouseClickLocation(Point mouse) {
		// showViewVolume(-32.0f, 32.0f, -32.0f, 32.0f, -32.0f, 32.0f);
		System.out.println(getWidth() + " " + getHeight());
		System.out.println(mouse.getX() + " " + mouse.getY());

		interpreter.consoleInput += mouse.getY() + " " + mouse.getX() + "\n";
		interpreter.consoleInput += "> ";

		// The point is currently placed to close to center
		// An offset has to be applied to compensate for perspective
		float x, y, z;
		x = (mouse.getX() - getWidth() / 2) / 5f;
		y = (getHeight() / 2 - mouse.getY()) / 5f;
		z = -32.f;

		// scene.manager.addSphere(new Point3f(x, y, -32.f),
		// new Point3i(255, 0, 0));
		// scene.manager.addSphere(new Point3f((x-400)/0.1, (300-y)/0.1, 0), new
		// Point3i(255, 0, 0));
		// scene.manager.addSphere(new Point3f((x-400)/32, (300-y)/32, 32.f),
		// new Point3i(255, 0, 0));
		Point3f point = new Point3f(x, y, z);
		return point;
	}

	private void userPickAt(Point mouse) {
		int[] selectBuf = new int[BUFFER_SIZE];
		IntBuffer selectBuffer = Buffers.newDirectIntBuffer(BUFFER_SIZE);
		int hits;
		int viewport[] = new int[4];
		gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);

		gl.glSelectBuffer(BUFFER_SIZE, selectBuffer);
		gl.glRenderMode(GL_SELECT);

		gl.glInitNames();
		gl.glPushName(0);

		gl.glMatrixMode(GL_PROJECTION);
		// gl.glPushMatrix();
		gl.glLoadIdentity();

		// 5x5 picking region
		glu.gluPickMatrix((double) mouse.getX(),
				(double) (viewport[3] - mouse.getY()), 5.0, 5.0, viewport, 0);
		float widthHeightRatio = (float) getWidth() / (float) getHeight();
		glu.gluPerspective(45, widthHeightRatio, 1, 1000);
		glu.gluLookAt(0, 0, cameraDistance, 0, 0, 0, 0, 1, 0);
		gl.glRotatef(cameraAngleInXZ, 0.f, 1.f, 0.f);

		gl.glRotatef(cameraAngleInYX,
				(float) Math.cos(Math.toRadians(cameraAngleInXZ)), 0.f,
				(float) Math.sin(Math.toRadians(cameraAngleInXZ)));

		scene.make(whiteMaterial, blackMaterial);

		// gl.glPopMatrix();
		gl.glMatrixMode(GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glFlush();

		hits = gl.glRenderMode(GL_RENDER);
		selectBuffer.get(selectBuf);
		processHits(hits, selectBuf);
	}

	private void processHits(int hits, int buffer[]) {
		int names, offset = 0;

		System.out.println("hits = " + hits);
		for (int i = 0; i < hits; i++) {
			names = buffer[offset];
			System.out.println(" number of names for hit = " + names);
			offset++;
			System.out.println("  z1 is " + buffer[offset]);
			offset++;
			System.out.println(" z2 is " + buffer[offset]);
			offset++;
			System.out.print("\n   the name is ");
			for (int j = 0; j < names; j++) {
				System.out.println("" + buffer[offset]);
				lastObject = buffer[offset];
				if (scene.selected.contains(lastObject))
					scene.selected.remove(lastObject);
				else
					scene.selected.add(lastObject);
				offset++;
			}
			System.out.println();
		}
	}

	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
			boolean deviceChanged) {

	}

	@Override
	public void dispose(GLAutoDrawable arg0) {
		// TODO Auto-generated method stub

	}

	private FloatBuffer make(float[] values) {
		FloatBuffer floatBuffer = Buffers.newDirectFloatBuffer(values.length);
		for (int i = 0; i < values.length; i++) {
			floatBuffer.put(values[i]);
		}
		floatBuffer.rewind();
		return floatBuffer;
	}
}