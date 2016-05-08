package org.freeims.logging.appender;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.CountingQuietWriter;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.spi.LoggingEvent;

public class CalendarFileAppender extends FileAppender {
	private static Logger logger = Logger.getLogger(CalendarFileAppender.class);

	/** 默认LOG文件超时时间 */
	private static final int DEFAULT_TIMEOUT_LOG_DAYS = 14;
	/**
	 * The default maximum file size is 10MB.
	 */
	protected long maxFileSize = 10 * 1024 * 1024;

	/**
	 * There is one backup file by default.
	 */
	protected int maxBackupIndex = 1;

	// The code assumes that the following constants are in a increasing
	// sequence.
	static final int TOP_OF_TROUBLE = -1;

	static final int TOP_OF_MINUTE = 0;

	static final int TOP_OF_HOUR = 1;

	static final int HALF_DAY = 2;

	static final int TOP_OF_DAY = 3;

	static final int TOP_OF_WEEK = 4;

	static final int TOP_OF_MONTH = 5;

	/**
	 * The date pattern. By default, the pattern is set to "'.'yyyy-MM-dd"
	 * meaning daily rollover.
	 */
	private String datePattern = "yyyy-MM-dd";

	private String scheduledDate;

	/**
	 * The next time we estimate a rollover should occur.
	 */
	private long nextCheck = System.currentTimeMillis() - 1;

	private String extname;

	private String path;

	private static int RollingFileType = 1;

	private static int DailyRollingFileType = 2;

	Date now = new Date();

	SimpleDateFormat sdf;

	CalendarRollingCalendar rc = new CalendarRollingCalendar();

	int checkPeriod = TOP_OF_TROUBLE;

	// The gmtTimeZone is used only in computeCheckPeriod() method.
	static final TimeZone gmtTimeZone = TimeZone.getTimeZone("GMT");

	/**
	 * The default constructor simply calls its
	 * {@link FileAppender#FileAppender parents constructor}.
	 */
	public CalendarFileAppender() {
		super();
	}

	/** LOG文件超时时间，在log4j.xml文件配置 */
	private int logTimeoutDays = DEFAULT_TIMEOUT_LOG_DAYS;

	/**
	 * Instantiate a RollingFileAppender and open the file designated by
	 * <code>filename</code>. The opened filename will become the ouput
	 * destination for this appender.
	 * 
	 * <p>
	 * If the <code>append</code> parameter is true, the file will be appended
	 * to. Otherwise, the file desginated by <code>filename</code> will be
	 * truncated before being opened.
	 */
	public CalendarFileAppender(Layout layout, String filename, boolean append) throws IOException {
		super(layout, filename, append);
	}

	/**
	 * Instantiate a FileAppender and open the file designated by
	 * <code>filename</code>. The opened filename will become the output
	 * destination for this appender.
	 * 
	 * <p>
	 * The file will be appended to.
	 */
	public CalendarFileAppender(Layout layout, String filename) throws IOException {
		super(layout, filename);
	}

	public CalendarFileAppender(Layout layout, String filename, String datePattern) throws IOException {
		super(layout, filename, true);
		this.datePattern = datePattern;
		activateOptions();
	}

	public void activateOptions() {
		if (datePattern != null && name != null) {
			now.setTime(System.currentTimeMillis());
			sdf = new SimpleDateFormat(datePattern);
			int type = computeCheckPeriod();
			printPeriodicity(type);
			rc.setType(type);
			fileName = this.buildLogFilePathAnddeleteTimeoutFile(-1);
			File file = new File(fileName);
			// Strfilename = String.format("[%s]%s_%d",
			// sdf.format(now),Strfilename,1);

			scheduledDate = sdf.format(new Date(file.lastModified()));
			/*
			 * scheduledFilename = String.format("[%s]%s.%s", sdf.format(new
			 * Date( file.lastModified())), name, extname);
			 */
			// Date(file.lastModified()));
		} else {
			LogLog.error("Either File or DatePattern options are not set for appender [" + name + "].");
		}
		super.activateOptions();
	}

	void printPeriodicity(int type) {
		switch (type) {
		case TOP_OF_MINUTE:
			LogLog.debug("Appender [" + name + "] to be rolled every minute.");
			break;
		case TOP_OF_HOUR:
			LogLog.debug("Appender [" + name + "] to be rolled on top of every hour.");
			break;
		case HALF_DAY:
			LogLog.debug("Appender [" + name + "] to be rolled at midday and midnight.");
			break;
		case TOP_OF_DAY:
			LogLog.debug("Appender [" + name + "] to be rolled at midnight.");
			break;
		case TOP_OF_WEEK:
			LogLog.debug("Appender [" + name + "] to be rolled at start of week.");
			break;
		case TOP_OF_MONTH:
			LogLog.debug("Appender [" + name + "] to be rolled at start of every month.");
			break;
		default:
			LogLog.warn("Unknown periodicity for appender [" + name + "].");
		}
	}

	// This method computes the roll over period by looping over the
	// periods, starting with the shortest, and stopping when the r0 is
	// different from from r1, where r0 is the epoch formatted according
	// the datePattern (supplied by the user) and r1 is the
	// epoch+nextMillis(i) formatted according to datePattern. All date
	// formatting is done in GMT and not local format because the test
	// logic is based on comparisons relative to 1970-01-01 00:00:00
	// GMT (the epoch).

	int computeCheckPeriod() {
		CalendarRollingCalendar rollingCalendar = new CalendarRollingCalendar(gmtTimeZone, Locale.ENGLISH);
		// set sate to 1970-01-01 00:00:00 GMT
		Date epoch = new Date(0);
		if (datePattern != null) {
			for (int i = TOP_OF_MINUTE; i <= TOP_OF_MONTH; i++) {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePattern);
				simpleDateFormat.setTimeZone(gmtTimeZone); // do all date
				// formatting in GMT
				String r0 = simpleDateFormat.format(epoch);
				rollingCalendar.setType(i);
				Date next = new Date(rollingCalendar.getNextCheckMillis(epoch));
				String r1 = simpleDateFormat.format(next);
				// logger.info("Type = "+i+", r0 = "+r0+", r1 = "+r1);
				if (r0 != null && r1 != null && !r0.equals(r1)) {
					return i;
				}
			}
		}
		return TOP_OF_TROUBLE; // Deliberately head for trouble...
	}

	/**
	 * Returns the value of the <b>MaxBackupIndex</b> option.
	 */
	public int getMaxBackupIndex() {
		return maxBackupIndex;
	}

	/**
	 * Get the maximum size that the output file is allowed to reach before
	 * being rolled over to backup files.
	 * 
	 * @since 1.1
	 */
	public long getMaximumFileSize() {
		return maxFileSize;
	}

	/**
	 * Implements the usual roll over behaviour.
	 * 
	 * <p>
	 * If <code>MaxBackupIndex</code> is positive, then files {
	 * <code>File.1</code>, ..., <code>File.MaxBackupIndex -1</code> are renamed
	 * to {<code>File.2</code>, ..., <code>File.MaxBackupIndex</code> .
	 * Moreover, <code>File</code> is renamed <code>File.1</code> and closed. A
	 * new <code>File</code> is created to receive further log output.
	 * 
	 * <p>
	 * If <code>MaxBackupIndex</code> is equal to zero, then the
	 * <code>File</code> is truncated with no backup files created.
	 * 
	 */
	public// synchronization not necessary since doAppend is alreasy synched
	void rollOver(int rolltype) throws IOException {
		//
		if (RollingFileType == rolltype) {
			File target;
			File file;
			LogLog.debug("rolling over count=" + ((CountingQuietWriter) qw).getCount());
			LogLog.debug("maxBackupIndex=" + maxBackupIndex);

			// If maxBackups <= 0, then there is no file renaming to be done.
			if (maxBackupIndex > 0) {
				file = new File(buildLogFilePathAnddeleteTimeoutFile(maxBackupIndex));
				if (file.exists())
					file.delete();
				// Map {(maxBackupIndex - 1), ..., 2, 1} to {maxBackupIndex,
				// ..., 3, 2}
				for (int i = maxBackupIndex - 1; i >= 1; i--) {
					file = new File(buildLogFilePathAnddeleteTimeoutFile(i));
					if (file.exists()) {
						target = new File(buildLogFilePathAnddeleteTimeoutFile(i + 1));
						LogLog.debug("Renaming file " + file + " to " + target);
						file.renameTo(target);
					}
				}

				// Rename fileName to fileName.1
				target = new File(buildLogFilePathAnddeleteTimeoutFile(1));

				this.closeFile(); // keep windows happy.

				file = new File(fileName);
				LogLog.debug("Renaming file " + file + " to " + target);
				file.renameTo(target);
			}

			try {
				// This will also close the file. This is OK since multiple
				// close operations are safe.
				this.setFile(fileName, false, bufferedIO, bufferSize);
			} catch (IOException e) {
				LogLog.error("setFile(" + fileName + ", false) call failed.", e);
			}
		} else if (DailyRollingFileType == rolltype) {
			/* Compute filename, but only if datePattern is specified */
			if (datePattern == null) {
				errorHandler.error("Missing DatePattern option in rollOver().");
				return;
			}
			String datedFile = sdf.format(now);
			// It is too early to roll over because we are still within the
			// bounds of the current interval. Rollover will occur once the
			// next interval is reached.
			if (scheduledDate.equals(datedFile)) {
				return;
			}
			// close current file, and rename it to datedFilename
			this.closeFile();
			String targetfilename = buildLogFilePathAnddeleteTimeoutFile(-1);

			fileName = targetfilename;

			try {
				// This will also close the file. This is OK since multiple
				// close operations are safe.
				this.setFile(fileName, true, this.bufferedIO, this.bufferSize);
			} catch (IOException e) {
				errorHandler.error("setFile(" + fileName + ", false) call failed.");
			}
			scheduledDate = datedFile;

		}

	}

	public synchronized void setFile(String fileName, boolean append, boolean bufferedIO, int bufferSize) throws IOException {
		super.setFile(fileName, append, this.bufferedIO, this.bufferSize);
		if (append) {
			File f = new File(fileName);
			((CountingQuietWriter) qw).setCount(f.length());
		}
	}

	/**
	 * Set the maximum number of backup files to keep around.
	 * 
	 * <p>
	 * The <b>MaxBackupIndex</b> option determines how many backup files are
	 * kept before the oldest is erased. This option takes a positive integer
	 * value. If set to zero, then there will be no backup files and the log
	 * file will be truncated when it reaches <code>MaxFileSize</code>.
	 */
	public void setMaxBackupIndex(int maxBackups) {
		this.maxBackupIndex = maxBackups;
	}

	/**
	 * Set the maximum size that the output file is allowed to reach before
	 * being rolled over to backup files.
	 * 
	 * <p>
	 * This method is equivalent to {@link #setMaxFileSize} except that it is
	 * required for differentiating the setter taking a <code>long</code>
	 * argument from the setter taking a <code>String</code> argument by the
	 * JavaBeans {@link java.beans.Introspector Introspector}.
	 * 
	 * @see #setMaxFileSize(String)
	 */
	public void setMaximumFileSize(long maxFileSize) {
		this.maxFileSize = maxFileSize;
	}

	/**
	 * Set the maximum size that the output file is allowed to reach before
	 * being rolled over to backup files.
	 * 
	 * <p>
	 * In configuration files, the <b>MaxFileSize</b> option takes an long
	 * integer in the range 0 - 2^63. You can specify the value with the
	 * suffixes "KB", "MB" or "GB" so that the integer is interpreted being
	 * expressed respectively in kilobytes, megabytes or gigabytes. For example,
	 * the value "10KB" will be interpreted as 10240.
	 */
	public void setMaxFileSize(String value) {
		maxFileSize = OptionConverter.toFileSize(value, maxFileSize + 1);
	}

	protected void setQWForFiles(Writer writer) {
		this.qw = new CountingQuietWriter(writer, errorHandler);
	}

	/**
	 * @return the logTimeoutDays
	 */
	public int getLogTimeoutDays() {
		return logTimeoutDays;
	}

	/**
	 * @param logTimeoutDays
	 *            the logTimeoutDays to set
	 */
	public void setLogTimeoutDays(int logTimeoutDays) {
		this.logTimeoutDays = logTimeoutDays;
	}

	/**
	 * This method differentiates RollingFileAppender from its super class.
	 * 
	 * @since 0.9.0
	 */
	protected void subAppend(LoggingEvent event) {
		long n = System.currentTimeMillis();
		if (n >= nextCheck) {
			now.setTime(n);
			nextCheck = rc.getNextCheckMillis(now);
			try {
				rollOver(DailyRollingFileType);
			} catch (IOException ioe) {
				LogLog.error("rollOver() failed.", ioe);
			}
		} else if ((fileName != null) && ((CountingQuietWriter) qw).getCount() >= maxFileSize) {
			try {
				rollOver(RollingFileType);
			} catch (IOException ioe) {
				LogLog.error("rollOver() failed.", ioe);
			}

		}

		super.subAppend(event);
	}

	public String getDatePattern() {
		return datePattern;
	}

	public void setDatePattern(String datePattern) {
		this.datePattern = datePattern;
	}

	public String getExtname() {
		return extname;
	}

	public void setExtname(String extname) {
		this.extname = extname;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	private synchronized void deleteFile(String filePath) {
		try {
			File file = new File(filePath);
			if (file.exists()) {
				if (file.delete())
					logger.warn("[Log]delete log file " + filePath);
			}
		} catch (Exception e) {
			logger.error("[Log]delete log file fail: " + filePath, e);
		}
	}

	/**
	 * 生成LOG文件,当index<0时删除过时文件
	 * 
	 * @author LIzhiyong 2011-1-21
	 * @param index
	 * @return
	 */
	private String buildLogFilePathAnddeleteTimeoutFile(int index) {
		String result = null;
		if (index >= 0) {
			result = path + "/" + String.format("[%s]%s_%d.%s", sdf.format(now), name, index, extname);
		} else {
			// 超时时间
			Calendar ca = Calendar.getInstance();
			ca.setTime(now);
			ca.add(Calendar.DAY_OF_MONTH, -1 * this.logTimeoutDays);
			result = path + "/" + String.format("[%s]%s.%s", sdf.format(now), name, extname);
			// 删除超时文件
			for (int i = 0; i <= maxBackupIndex; ++i) {
				String timeoutFilePath = null;
				if (i == 0) {
					timeoutFilePath = path + "/" + String.format("[%s]%s.%s", sdf.format(ca.getTime()), name, extname);
				} else {
					timeoutFilePath = path + "/" + String.format("[%s]%s_%d.%s", sdf.format(ca.getTime()), name, i, extname);
				}
				this.deleteFile(timeoutFilePath);
			}
		}
		return result;
	}
};

/**
 * RollingCalendar is a helper class to DailyRollingFileAppender. Given a
 * periodicity type and the current time, it computes the start of the next
 * interval.
 */
class CalendarRollingCalendar extends GregorianCalendar {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8011676535294195669L;

	private int type = CalendarFileAppender.TOP_OF_TROUBLE;

	CalendarRollingCalendar() {
		super();
	}

	CalendarRollingCalendar(TimeZone tz, Locale locale) {
		super(tz, locale);
	}

	void setType(int type) {
		this.type = type;
	}

	public long getNextCheckMillis(Date now) {
		return getNextCheckDate(now).getTime();
	}

	public Date getNextCheckDate(Date now) {
		this.setTime(now);

		switch (type) {
		case CalendarFileAppender.TOP_OF_MINUTE:
			this.set(Calendar.SECOND, 0);
			this.set(Calendar.MILLISECOND, 0);
			this.add(Calendar.MINUTE, 1);
			break;
		case CalendarFileAppender.TOP_OF_HOUR:
			this.set(Calendar.MINUTE, 0);
			this.set(Calendar.SECOND, 0);
			this.set(Calendar.MILLISECOND, 0);
			this.add(Calendar.HOUR_OF_DAY, 1);
			break;
		case CalendarFileAppender.HALF_DAY:
			this.set(Calendar.MINUTE, 0);
			this.set(Calendar.SECOND, 0);
			this.set(Calendar.MILLISECOND, 0);
			int hour = get(Calendar.HOUR_OF_DAY);
			if (hour < 12) {
				this.set(Calendar.HOUR_OF_DAY, 12);
			} else {
				this.set(Calendar.HOUR_OF_DAY, 0);
				this.add(Calendar.DAY_OF_MONTH, 1);
			}
			break;
		case CalendarFileAppender.TOP_OF_DAY:
			this.set(Calendar.HOUR_OF_DAY, 0);
			this.set(Calendar.MINUTE, 0);
			this.set(Calendar.SECOND, 0);
			this.set(Calendar.MILLISECOND, 0);
			this.add(Calendar.DATE, 1);
			break;
		case CalendarFileAppender.TOP_OF_WEEK:
			this.set(Calendar.DAY_OF_WEEK, getFirstDayOfWeek());
			this.set(Calendar.HOUR_OF_DAY, 0);
			this.set(Calendar.SECOND, 0);
			this.set(Calendar.MILLISECOND, 0);
			this.add(Calendar.WEEK_OF_YEAR, 1);
			break;
		case CalendarFileAppender.TOP_OF_MONTH:
			this.set(Calendar.DATE, 1);
			this.set(Calendar.HOUR_OF_DAY, 0);
			this.set(Calendar.SECOND, 0);
			this.set(Calendar.MILLISECOND, 0);
			this.add(Calendar.MONTH, 1);
			break;
		default:
			throw new IllegalStateException("Unknown periodicity type.");
		}
		return getTime();
	}

};