package jjil.algorithm;

import jjil.core.Error;
import jjil.core.Gray8Image;
import jjil.core.Image;
import jjil.core.PipelineStage;

public class Gray8Unsigned2Signed extends PipelineStage {

	public void push(Image imageInput) throws Error {
		if (!(imageInput instanceof Gray8Image)) {
			throw new Error(Error.PACKAGE.ALGORITHM,
					ErrorCodes.IMAGE_NOT_GRAY8IMAGE, imageInput.toString(),
					null, null);
		}
		Gray8Image gray = (Gray8Image) imageInput;
		byte[] rb = gray.getData();
		for (int i=0; i<gray.getWidth()*gray.getHeight(); i++) {
			rb[i] = (byte) ((0xff&rb[i]) + Byte.MIN_VALUE);
		}
		super.setOutput(gray);
	}

}
