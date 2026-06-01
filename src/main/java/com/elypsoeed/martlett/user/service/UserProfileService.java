package com.elypsoeed.martlett.user.service;

import com.elypsoeed.martlett.common.entity.UserEntity;
import com.elypsoeed.martlett.common.model.Sex;
import com.elypsoeed.martlett.common.repository.UserRepository;
import com.elypsoeed.martlett.generated.model.UpdateUserProfileRequest;
import com.elypsoeed.martlett.user.exception.InvalidUserAvatarException;
import com.elypsoeed.martlett.user.exception.UserProfileNotFoundException;
import com.elypsoeed.martlett.user.model.UserAvatar;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;

@Service
@RequiredArgsConstructor
public class UserProfileService {

	private static final long MAX_AVATAR_SIZE_BYTES = 2L * 1024 * 1024;
	private static final int AVATAR_SIZE_PIXELS = 256;
	private static final float JPEG_QUALITY = 0.85F;
	private static final String AVATAR_CONTENT_TYPE = "image/jpeg";

	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public UserEntity getByUsername(String username) {
		return userRepository.findByUsername(username)
			.orElseThrow(UserProfileNotFoundException::new);
	}

	@Transactional
	public UserEntity updateByUsername(String username, UpdateUserProfileRequest updateUserProfileRequest) {
		UserEntity user = userRepository.findByUsername(username)
			.orElseThrow(UserProfileNotFoundException::new);

		user.setFirstName(updateUserProfileRequest.getFirstName());
		user.setLastName(updateUserProfileRequest.getLastName());
		user.setSex(mapSex(updateUserProfileRequest));
		user.setAge(updateUserProfileRequest.getAge());
		user.setCity(updateUserProfileRequest.getCity());
		user.setTgContact(updateUserProfileRequest.getTgContact());
		user.setEmailContact(updateUserProfileRequest.getEmailContact());
		user.setPlaceOfWork(updateUserProfileRequest.getPlaceOfWork());

		return userRepository.save(user);
	}

	@Transactional
	public UserEntity updateAvatar(String username, MultipartFile file) {
		validateAvatar(file);

		UserEntity user = userRepository.findByUsername(username)
			.orElseThrow(UserProfileNotFoundException::new);

		user.setAvatarContentType(AVATAR_CONTENT_TYPE);
		user.setAvatarData(normalizeAvatar(file));

		return userRepository.save(user);
	}

	@Transactional(readOnly = true)
	public UserAvatar getAvatar(String username) {
		UserEntity user = userRepository.findByUsername(username)
			.orElseThrow(UserProfileNotFoundException::new);

		if (user.getAvatarData() == null || user.getAvatarContentType() == null) {
			throw new UserProfileNotFoundException();
		}

		return new UserAvatar(user.getAvatarContentType(), user.getAvatarData());
	}

	private Sex mapSex(UpdateUserProfileRequest updateUserProfileRequest) {
		if (updateUserProfileRequest.getSex() == null) {
			return null;
		}

		return Sex.valueOf(updateUserProfileRequest.getSex().getValue());
	}

	private void validateAvatar(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new InvalidUserAvatarException("Avatar file is required");
		}
		if (file.getSize() > MAX_AVATAR_SIZE_BYTES) {
			throw new InvalidUserAvatarException("Avatar file is too large");
		}
		if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
			throw new InvalidUserAvatarException("Avatar file must be an image");
		}
	}

	private byte[] normalizeAvatar(MultipartFile file) {
		BufferedImage source = readImage(file);
		BufferedImage avatar = new BufferedImage(
			AVATAR_SIZE_PIXELS,
			AVATAR_SIZE_PIXELS,
			BufferedImage.TYPE_INT_RGB
		);

		int cropSize = Math.min(source.getWidth(), source.getHeight());
		int cropX = (source.getWidth() - cropSize) / 2;
		int cropY = (source.getHeight() - cropSize) / 2;

		Graphics2D graphics = avatar.createGraphics();
		try {
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0, 0, AVATAR_SIZE_PIXELS, AVATAR_SIZE_PIXELS);
			graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			graphics.drawImage(
				source,
				0,
				0,
				AVATAR_SIZE_PIXELS,
				AVATAR_SIZE_PIXELS,
				cropX,
				cropY,
				cropX + cropSize,
				cropY + cropSize,
				null
			);
		} finally {
			graphics.dispose();
		}

		return encodeJpeg(avatar);
	}

	private BufferedImage readImage(MultipartFile file) {
		try {
			BufferedImage source = ImageIO.read(file.getInputStream());
			if (source == null) {
				throw new InvalidUserAvatarException("Avatar file must be a supported image");
			}
			return source;
		} catch (IOException exception) {
			throw new InvalidUserAvatarException("Failed to read avatar image");
		}
	}

	private byte[] encodeJpeg(BufferedImage avatar) {
		Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
		if (!writers.hasNext()) {
			throw new IllegalStateException("JPEG image writer is not available");
		}

		ImageWriter writer = writers.next();
		try (ByteArrayOutputStream output = new ByteArrayOutputStream();
			 ImageOutputStream imageOutput = ImageIO.createImageOutputStream(output)) {
			ImageWriteParam writeParam = writer.getDefaultWriteParam();
			if (writeParam.canWriteCompressed()) {
				writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
				writeParam.setCompressionQuality(JPEG_QUALITY);
			}

			writer.setOutput(imageOutput);
			writer.write(null, new IIOImage(avatar, null, null), writeParam);
			return output.toByteArray();
		} catch (IOException exception) {
			throw new UncheckedIOException("Failed to encode user avatar", exception);
		} finally {
			writer.dispose();
		}
	}
}
