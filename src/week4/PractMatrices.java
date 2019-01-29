package week4;

import graphicslib3D.*;

public class PractMatrices {
	
	private MatrixStack myStack = new MatrixStack(20);
	
	public PractMatrices() {
		
	}
	
	public Matrix3D pRotate(double angle) {
		
		//generairen von Einheitmatrix
		Matrix3D rotationsMatrix = new Matrix3D();
		
		//rotation um angle
		 rotationsMatrix.rotateZ(angle);
		 return rotationsMatrix;
	}
	
	public Vector3D vecRotation(Vector3D inVec , double angle) {
		//generairen von Einheitmatrix
		Matrix3D rotationsMatrix = new Matrix3D();
		 rotationsMatrix.rotateZ(angle);
		 
		 return inVec.mult(rotationsMatrix);
	}

	public static void main(String[] args) {

		// vector verschiebung
		
		Matrix3D translationMatrix = new Matrix3D();
		System.out.println(translationMatrix);
		
		
		//Einheitsmatrix 4.Spalte verändern (translate) mit Input Para
		translationMatrix.translate(1.0f, 0.0f, 0.0f);
		System.out.println(translationMatrix);
		
		//anlegen von Vector
		Vector3D mVector = new Vector3D(2.0f,1.0f,4.0f, 1.0f);
		System.out.println(mVector);

		// multiplication von vector und translation matrix
		Vector3D translatedVector =  mVector.mult(translationMatrix);

		System.out.println(translatedVector);
		System.out.println("\nRotation: ");

		
		/// Rotation
		PractMatrices p = new PractMatrices();
		Matrix3D res = p.pRotate(90);
		System.out.println(res);
		
		
		System.out.println("\nVector rotation: ");
		Vector3D vec1 = new Vector3D(1.0f,0.0f,0.0f, 0.0f);
		Vector3D outputVec = p.vecRotation(vec1,30);
		
		System.out.print(outputVec);
		
		
		//Persektiven der Kamera
		System.out.println("\n Denfinieren von selbst zu beschriebener Matrix: ");
		Matrix3D r = new Matrix3D();
		//Lesen von Kapitel 4 in OpenGL Buch
		// setze die Elemente der Matrix zu den value des letzten Para
		r.setElementAt(0, 0, 10);
		r.setElementAt(1, 1, 20);
		r.setElementAt(2, 2, 30);
		r.setElementAt(3, 3, 40);
		r.setElementAt(0, 3, 5);
		r.setElementAt(1, 3, 10);
		r.setElementAt(2, 3, 15);
		System.out.println(r);
		
		System.out.println("\n **** MatrixStack ***** ");
		System.out.println("\n Anzeigen des ersten Elementes des Stacks ");

		// MatrixStack halt als erstes Element eine Einheitsmatrix per default
		System.out.println(p.myStack.peek());
		
		//push einer copy der Einheitsmatrix auf den Stack -> es sind nun 2 Elemente auf den Stack
		System.out.println("copy erzeugen von Einheitsmatrix");

		p.myStack.pushMatrix();
		System.out.println(p.myStack.peek());

		p.myStack.multMatrix(r);
		System.out.println(p.myStack.peek());
		
		// check Buch Seite 97 Beispeil programm 
		
	}

}
