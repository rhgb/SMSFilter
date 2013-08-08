package org.monospace.smsfilter;

/**
 * @author rhgb
 *         13-7-31
 */
public class DbVars {
	public static final String COL_ID = "_id";
	public static final String TABLE_FILTER = "filter";
	public static final String COL_FIL_TARGET = "target";
	public static final String COL_FIL_TYPE = "type";
	public static final String COL_FIL_RULE = "rule";
	public static final String COL_FIL_STATE = "state";
	public static final String COL_FIL_DESC = "description";
	public static final String TABLE_SMS = "sms";
	public static final String COL_SMS_SENDER = "address";
	public static final String COL_SMS_CONTENT = "message";
	public static final String COL_SMS_RECV_TIME = "receive_time";
	public static final String COL_SMS_STATE = "state";

	public enum FilterType {
		ADDR_START_WITH(0, FilterType.TARGET_ADDR, FilterType.TYPE_START_WITH),
		ADDR_CONTAINS(1, FilterType.TARGET_ADDR, FilterType.TYPE_CONTAINS),
		ADDR_END_WITH(2, FilterType.TARGET_ADDR, FilterType.TYPE_END_WITH),
		CONTENT_CONTAINS(3, FilterType.TARGET_CONTENT, FilterType.TYPE_CONTAINS),
		ADDR_RAW(4, FilterType.TARGET_ADDR, FilterType.TYPE_RAW),
		CONTENT_RAW(5, FilterType.TARGET_CONTENT, FilterType.TYPE_RAW);

		private final int index;
		private final String target;
		private final String type;

		public static final String TARGET_ADDR = "address";
		public static final String TARGET_CONTENT = "content";
		public static final String TYPE_START_WITH = "start_with";
		public static final String TYPE_END_WITH = "end_with";
		public static final String TYPE_CONTAINS = "contains";
		public static final String TYPE_RAW = "raw";

		private FilterType(int i, String tar, String type) {
			this.index = i;
			this.target = tar;
			this.type = type;
		}

		public static FilterType get(int i) {
			for (FilterType t : values()) {
				if (i == t.index) return t;
			}
			return null;
		}

		public String getTarget() {
			return target;
		}

		public String getType() {
			return type;
		}

		public String applyContent(String src) {
			switch (this) {
				case ADDR_RAW:
				case CONTENT_RAW:
					return src;
				case ADDR_START_WITH:
					return src + "%";
				case ADDR_END_WITH:
					return "%" + src;
				case ADDR_CONTAINS:
				case CONTENT_CONTAINS:
					return "%" + src + "%";
			}
			return src;
		}
	}

	public enum FilterState {
		BLOCK(0, FilterState.STATE_BLOCK),
		PERMIT(1, FilterState.STATE_PERMIT);

		public static final String STATE_BLOCK = "block";
		public static final String STATE_PERMIT = "permit";

		private final int index;
		private final String key;

		private FilterState(int i, String k) {
			this.index = i;
			this.key = k;
		}

		public static FilterState get(int i) {
			for (FilterState s : values()) {
				if (i == s.index) return s;
			}
			return null;
		}

		public static FilterState get(String key) {
			for (FilterState s : values()) {
				if (key.equals(s.key)) return s;
			}
			return null;
		}

		@Override
		public String toString() {
			return key;
		}
	}
}
