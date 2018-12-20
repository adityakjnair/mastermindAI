package ai.project.game;

public class SecretCodeGenerator {
	/**
	 * TODO Do we provide user an option to set the code as well rather than us
	 * generating it? May be the generator can accept a generation strategy instead
	 * of forcing random code generation. Also need to think about allowing
	 * duplicates.
	 * 
	 * Method generating the secret code.
	 * 
	 * @param codeLen
	 * @param colors
	 * @return
	 */
	public static int[] generate(int codeLen, int colors) {
		 int[] code = new int[] { 7, 8, 8, 5, 3, 0 };
//		 int[] code = new int[] { 2, 1, 4, 4, 2, 5};
//		int[] code = new int[codeLen];
//		int i = 0;
//		while (i < codeLen) {
//			code[i] = (int) (Math.random() * (colors - 1) + 1);
//			i++;
//		}
		return code;
	}

}
