package fr.be.your.self.backend.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import fr.be.your.self.common.UserPermission;

public class PermissionDto implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5317213474474452007L;
	
	private Map<String, Integer> userPermissions = new HashMap<>();

	public boolean hasPermission(String... paths) {
		if (paths == null || paths.length == 0) {
			return false;
		}

		for (int i = 0; i < paths.length; i++) {
			final String path = paths[i];
			final String funcPath = (path.startsWith("/") ? "" : "/") + path.toLowerCase();
			final Integer permission = this.userPermissions.get(funcPath);

			if (permission != null && (permission == UserPermission.READONLY.getValue()
					|| permission == UserPermission.WRITE.getValue())) {
				return true;
			}
		}

		return false;
	}

	public boolean hasWritePermission(String... paths) {
		if (paths == null || paths.length == 0) {
			return false;
		}

		for (int i = 0; i < paths.length; i++) {
			final String path = paths[i];
			final String funcPath = (path.startsWith("/") ? "" : "/") + path.toLowerCase();
			final Integer permission = this.userPermissions.get(funcPath);

			if (permission != null && permission == UserPermission.WRITE.getValue()) {
				return true;
			}
		}

		return false;
	}

	public int getPermission(String path) {
		if (path == null || path.isEmpty()) {
			return UserPermission.DENIED.getValue();
		}

		final String funcPath = (path.startsWith("/") ? "" : "/") + path.toLowerCase();
		final Integer permission = this.userPermissions.get(funcPath);

		if (permission == null) {
			return UserPermission.DENIED.getValue();
		}

		return permission.intValue();
	}

	public void addPermission(String path, int permission) {
		this.userPermissions.put(path.toLowerCase(), permission);
	}
}
