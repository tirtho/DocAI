package com.azure.spring.samples.model;

import java.util.ArrayList;
import java.util.Objects;

public class Category {

		private String category;
		private Double confidence;
		private ArrayList<Integer> pages;
		
		public Category() {
			super();
			// TODO Auto-generated constructor stub
		}

		public Category(String category, Double confidence, ArrayList<Integer> pages) {
			super();
			this.category = category;
			this.confidence = confidence;
			this.pages = pages;
		}

		public String getCategory() {
			return category;
		}

		public void setCategory(String category) {
			this.category = category;
		}

		public Double getConfidence() {
			return confidence;
		}

		public void setConfidence(Double confidence) {
			this.confidence = confidence;
		}

		public ArrayList<Integer> getPages() {
			return pages;
		}

		public void setPages(ArrayList<Integer> pages) {
			this.pages = pages;
		}

		@Override
		public int hashCode() {
			return Objects.hash(category, confidence, pages);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Category other = (Category) obj;
			return Objects.equals(category, other.category) && Objects.equals(confidence, other.confidence)
					&& Objects.equals(pages, other.pages);
		}

		@Override
		public String toString() {
			return "DataCategory [category=" + category + ", confidence=" + confidence + ", pages=" + pages + "]";
		}
		
		
}
