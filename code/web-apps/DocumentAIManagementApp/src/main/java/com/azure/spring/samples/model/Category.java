package com.azure.spring.samples.model;

import java.util.Objects;

public class Category {

		private String category;
		private Double confidence;
		
		public Category() {
			super();
			// TODO Auto-generated constructor stub
		}
		
		public Category(String category, Double confidence) {
			super();
			this.category = category;
			this.confidence = confidence;
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
		@Override
		public int hashCode() {
			return Objects.hash(category, confidence);
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
			return Objects.equals(category, other.category) && Objects.equals(confidence, other.confidence);
		}
		@Override
		public String toString() {
			return "Category [category=" + category + ", confidence=" + confidence + "]";
		}
				
}
