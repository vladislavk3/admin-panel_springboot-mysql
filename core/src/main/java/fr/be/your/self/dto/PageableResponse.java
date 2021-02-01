package fr.be.your.self.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class PageableResponse<T> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1439444370500627013L;

	private List<T> items;
	private long totalItems;

	private int pageIndex;
	private int pageSize;
	private long totalPages;

	public static PageableResponse<?> emptyPageableResponse() {
		return new PageableResponse<>(Collections.emptyList());
	}

	public PageableResponse() {
		super();

		this.pageIndex = 0;
		this.pageSize = -1;
		this.totalPages = 0;
	}

	public PageableResponse(List<T> items) {
		this();

		this.items = items;

		if (items != null) {
			this.totalItems = items.size();

			if (this.totalItems > 0) {
				this.pageIndex = 1;
				this.totalPages = 1;
			}
		}
	}

	public PageableResponse(Page<T> page) {
		this();

		if (page != null) {
			final Pageable pageable = page.getPageable();

			this.items = page.getContent();
			this.totalItems = page.getTotalElements();
			this.totalPages = page.getTotalPages();
			this.pageIndex = pageable.getPageNumber() + 1;
			this.pageSize = pageable.getPageSize();
		}
	}

	public PageableResponse(List<T> items, int count) {
		this();

		this.items = items;
		this.totalItems = count;
	}

	public PageableResponse(List<T> items, long count) {
		this();

		this.items = items;
		this.totalItems = count;
	}

	public List<T> getItems() {
		return items;
	}

	public void setItems(List<T> items) {
		this.items = items;
	}

	public void addItem(T item) {
		if (item == null) {
			return;
		}

		if (this.items == null) {
			this.items = new ArrayList<>();
		}

		this.items.add(item);
	}

	public long getTotalItems() {
		return totalItems;
	}

	public void setTotalItems(long totalItems) {
		this.totalItems = totalItems;
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public long getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(long totalPages) {
		this.totalPages = totalPages;
	}
}
