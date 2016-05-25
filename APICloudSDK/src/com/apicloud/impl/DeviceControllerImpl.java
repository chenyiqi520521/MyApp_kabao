package com.apicloud.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;








import android.os.Message;
import android.util.Log;

import com.apicloud.common.AppExCode;
import com.apicloud.common.Common;
import com.apicloud.common.Const1.DataEncryptWKIndexConst;
import com.apicloud.common.Const1.KeyIndexConst;
import com.apicloud.common.Const1.MKIndexConst;
import com.apicloud.common.Const1.MacWKIndexConst;
import com.apicloud.common.Const1.PinWKIndexConst;
import com.apicloud.controller.DeviceController;
import com.apicloud.controller.DeviceListener;
import com.apicloud.controller.TransferListener;
import com.newland.me.ConnUtils;
import com.newland.me.DeviceManager;
import com.newland.me.DeviceManager.DeviceConnState;
import com.newland.mtype.BatteryInfoResult;
import com.newland.mtype.ConnectionCloseEvent;
import com.newland.mtype.Device;
import com.newland.mtype.DeviceInfo;
import com.newland.mtype.DeviceOutofLineException;
import com.newland.mtype.DeviceRTException;
import com.newland.mtype.ModuleType;
import com.newland.mtype.common.ExCode;
import com.newland.mtype.common.MESeriesConst.TrackEncryptAlgorithm;
import com.newland.mtype.conn.DeviceConnParams;
import com.newland.mtype.event.AbstractProcessDeviceEvent;
import com.newland.mtype.event.DeviceEvent;
import com.newland.mtype.event.DeviceEventListener;
import com.newland.mtype.log.DeviceLogger;
import com.newland.mtype.log.DeviceLoggerFactory;
import com.newland.mtype.module.common.cardreader.CardReader;
import com.newland.mtype.module.common.cardreader.CardReaderResult;
import com.newland.mtype.module.common.cardreader.CardRule;
import com.newland.mtype.module.common.cardreader.OpenCardReaderEvent;
import com.newland.mtype.module.common.cardreader.OpenCardType;
import com.newland.mtype.module.common.emv.EmvModule;
import com.newland.mtype.module.common.emv.EmvTransController;
import com.newland.mtype.module.common.emv.QPBOCModule;
import com.newland.mtype.module.common.keyboard.KeyBoard;
import com.newland.mtype.module.common.keyboard.KeyBoardReadingEvent;
import com.newland.mtype.module.common.lcd.DispType;
import com.newland.mtype.module.common.lcd.LCD;
import com.newland.mtype.module.common.pin.AccountInputType;
import com.newland.mtype.module.common.pin.EncryptType;
import com.newland.mtype.module.common.pin.KekUsingType;
import com.newland.mtype.module.common.pin.MacAlgorithm;
import com.newland.mtype.module.common.pin.PinInput;
import com.newland.mtype.module.common.pin.PinInputEvent;
import com.newland.mtype.module.common.pin.PinManageType;
import com.newland.mtype.module.common.pin.WorkingKey;
import com.newland.mtype.module.common.pin.WorkingKeyType;
import com.newland.mtype.module.common.printer.Printer;
import com.newland.mtype.module.common.swiper.SwipResult;
import com.newland.mtype.module.common.swiper.SwipResultType;
import com.newland.mtype.module.common.swiper.Swiper;
import com.newland.mtype.module.common.swiper.SwiperReadModel;
import com.newland.mtype.module.common.swiper.TrackSecurityPaddingType;
import com.newland.mtype.module.external.me11.ME11External;
import com.newland.mtype.module.external.me11.ME11SwipResult;
import com.newland.mtype.tlv.TLVPackage;
import com.newland.mtype.util.Dump;
import com.newland.mtype.util.ISOUtils;

/**
 * 具体实现类
 * 
 * 
 */
public class DeviceControllerImpl implements DeviceController {

	private DeviceLogger logger = DeviceLoggerFactory
			.getLogger(DeviceControllerImpl.class);

	private static DeviceManager deviceManager = ConnUtils.getDeviceManager();

	private DeviceConnParams connParams;
	private String driverName;

	private DeviceControllerImpl() {
	}

	public void init(Context context, String driverName,
			DeviceConnParams params,
			DeviceEventListener<ConnectionCloseEvent> listener) {
		deviceManager.init(context, driverName, params, listener);
		this.connParams = params;
		this.driverName = driverName;
	}

	public DeviceConnParams getDeviceConnParams() {
		Device device = deviceManager.getDevice();
		if (device == null)
			return null;

		return (DeviceConnParams) device.getBundle();
	}

	public static DeviceController getInstance() {
		return new DeviceControllerImpl();
	}

	@Override
	public void connect() throws Exception {
		deviceManager.connect();
		deviceManager.getDevice().setBundle(connParams);
	}

	@Override
	public void disConnect() {
		deviceManager.disconnect();
		
	}

	@Override
	public void updateWorkingKey(WorkingKeyType workingKeyType,
			byte[] encryData, byte[] checkValue) {
		PinInput pinInput = (PinInput) deviceManager.getDevice()
				.getStandardModule(ModuleType.COMMON_PININPUT);
		int mkIndex = MKIndexConst.DEFAULT_MK_INDEX;

		byte[] rslt = null;
		switch (workingKeyType) {
		case PININPUT:
			rslt = pinInput.loadWorkingKey(WorkingKeyType.PININPUT, mkIndex,
					PinWKIndexConst.DEFAULT_PIN_WK_INDEX, encryData);
			break;
		case DATAENCRYPT:
			rslt = pinInput.loadWorkingKey(WorkingKeyType.DATAENCRYPT, mkIndex,
					DataEncryptWKIndexConst.DEFAULT_TRACK_WK_INDEX, encryData);
			break;
		case MAC:
			rslt = pinInput.loadWorkingKey(WorkingKeyType.MAC, mkIndex,
					MacWKIndexConst.DEFAULT_MAC_WK_INDEX, encryData);
			break;
		default:
			throw new DeviceRTException(AppExCode.LOAD_WORKINGKEY_FAILED,
					"unknown key type!" + workingKeyType);
		}
		byte[] expectedKcv = new byte[4];
		System.arraycopy(rslt, 0, expectedKcv, 0, expectedKcv.length);
		if (!Arrays.equals(expectedKcv, checkValue)) {
			throw new RuntimeException("failed to check kcv!:["
					+ Dump.getHexDump(expectedKcv) + ","
					+ Dump.getHexDump(checkValue) + "]");
		}
	}

	

	@Override
	public PinInputEvent startPininput(AccountInputType acctInputType,
			String acctHash, int inputMaxLen, boolean isEnterEnabled,
			String msg, long timeout) throws InterruptedException {
		isConnected();
		PinInput pinInput = (PinInput) deviceManager.getDevice()
				.getStandardModule(ModuleType.COMMON_PININPUT);
		EventHolder<PinInputEvent> listener = new EventHolder<PinInputEvent>();
		pinInput.startStandardPinInput(new WorkingKey(
				KeyIndexConst.KSN_INITKEY_INDEX), PinManageType.DUKPT,
				acctInputType, acctHash, inputMaxLen, new byte[] { 'F', 'F',
						'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F' },
				isEnterEnabled, msg, (int) timeout, TimeUnit.MILLISECONDS,
				listener);
		try {
			listener.startWait();
		} catch (InterruptedException e) {
			pinInput.cancelPinInput();
			throw e;
		} finally {
			// clearScreen();
		}
		PinInputEvent event = listener.event;
		event = preEvent(event, AppExCode.GET_PININPUT_FAILED);
		if (event == null) {
			logger.info("start getChipherText,but return is none!may user canceled?");
			return null;
		}
		return event;
	}

	@Override
	public void showMessage(String msg) {
		LCD lcd = (LCD) deviceManager.getDevice().getStandardModule(
				ModuleType.COMMON_LCD);
		if (lcd != null) {
			lcd.draw(msg);
		}
	}

	@Override
	public void clearScreen() {
		LCD lcd = (LCD) deviceManager.getDevice().getStandardModule(
				ModuleType.COMMON_LCD);
		if (lcd != null) {
			lcd.clearScreen();
		}
	}
	
	//获取通用刷卡类型
	/*@Override
	private ModuleType getSwipType(final String msg, final long timeout, final TimeUnit timeunit){
		    ModuleType type;
		    CardReader cardReader = (CardReader) deviceManager.getDevice().getStandardModule(ModuleType.COMMON_CARDREADER);
		    EventHolder<OpenCardReaderEvent> listener = new EventHolder<OpenCardReaderEvent>();
			cardReader.openCardReader(msg, new ModuleType[] { ModuleType.COMMON_SWIPER, ModuleType.COMMON_ICCARD }, timeout, timeunit, listener);
			try {
				listener.startWait();
			} catch (InterruptedException e) {
				cardReader.cancelCardRead();
			} finally {
				clearScreen();
			}
			OpenCardReaderEvent event = listener.event;
			event = preEvent(event, AppExCode.GET_TRACKTEXT_FAILED);
			if (event == null) {
				throw new DeviceRTException(AppExCode.GET_TRACKTEXT_FAILED, "no event accept.");
			}
			ModuleType[] openedModuleTypes = event.getOpenedCardReaders();
			type=openedModuleTypes[0];
		return type;
	}*/
	private SwipResult getSwipResult(Swiper swiper, int trackKey, String encryptType, int flag) {
		isConnected();
		SwipResult swipRslt;
		if (flag == Const.CardType.COMMON) {
			swipRslt = swiper.readEncryptResult(new SwiperReadModel[] { SwiperReadModel.READ_SECOND_TRACK, SwiperReadModel.READ_THIRD_TRACK }, new WorkingKey(trackKey), encryptType);
		} else {
			swipRslt = swiper.readEncryptResult(new SwiperReadModel[] { SwiperReadModel.READ_IC_SECOND_TRACK }, new WorkingKey(trackKey), encryptType);
		}
		return swipRslt;
	}
	@Override
	public SwipResult swipCommonCard(String msg, long timeout, TimeUnit timeUnit) {
		CardReader cardReader = (CardReader) deviceManager.getDevice().getStandardModule(ModuleType.COMMON_CARDREADER);
		if (cardReader == null) {
			throw new DeviceRTException(AppExCode.GET_TRACKTEXT_FAILED, "not support read card!");
		}
		try {
			ModuleType[] openedModuleTypes = cardReader.openCardReader(msg, new ModuleType[] { ModuleType.COMMON_SWIPER }, 30, TimeUnit.SECONDS);
			if (openedModuleTypes == null || openedModuleTypes.length <= 0) {
				logger.info("start cardreader,but return is none!may user canceled?");
				return null;
			}
			if (openedModuleTypes.length > 1) {
				logger.warn("should return only one type of cardread action!but is " + openedModuleTypes.length);
				throw new DeviceRTException(AppExCode.GET_TRACKTEXT_FAILED, "should return only one type of cardread action!but is " + openedModuleTypes.length);
			}
			switch (openedModuleTypes[0]) {
			case COMMON_SWIPER:
				Swiper swiper = (Swiper) deviceManager.getDevice().getStandardModule(ModuleType.COMMON_SWIPER);
				SwipResult swipRslt = getSwipResult(swiper, DataEncryptWKIndexConst.DEFAULT_TRACK_WK_INDEX, TrackEncryptAlgorithm.BY_UNIONPAY_MODEL, 0);
				if (swipRslt.getRsltType() == SwipResultType.SUCCESS) {
					return swipRslt;
				}
				throw new DeviceRTException(AppExCode.GET_TRACKTEXT_FAILED, "" + swipRslt.getRsltType());
			default:
				throw new DeviceRTException(AppExCode.GET_TRACKTEXT_FAILED, "not support cardreader module:" + openedModuleTypes[0]);
			}
		} finally {
			cardReader.closeCardReader();
		}
	}
	@Override
	public ME11SwipResult swipCard(String msg, long timeout, TimeUnit timeUnit) {
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyMMddHHmmss");
		String tradingtime=sDateFormat.format(new Date(System.currentTimeMillis()));		
		ME11External me11Model = (ME11External) deviceManager.getDevice().getExModule(ME11External.MODULE_NAME);
		ME11SwipResult swipeResult =null;
		if(me11Model!=null){
			 swipeResult = me11Model.openCardReader(new ModuleType[] { ModuleType.COMMON_SWIPER, ModuleType.COMMON_ICCARD }, timeout, timeUnit,
					new SwiperReadModel[] { SwiperReadModel.READ_SECOND_TRACK, SwiperReadModel.READ_THIRD_TRACK }, (byte)0xff, 
					TrackEncryptAlgorithm.BY_M10_MODEL, new WorkingKey(DataEncryptWKIndexConst.DEFAULT_TRACK_WK_INDEX), new byte[] {Integer.valueOf(tradingtime.substring(0, 2), 16).byteValue(), 			
				Integer.valueOf(tradingtime.substring(2, 4), 16).byteValue(), 
				Integer.valueOf(tradingtime.substring(4, 6), 16).byteValue(), 
				Integer.valueOf(tradingtime.substring(6, 8), 16).byteValue(),
				Integer.valueOf(tradingtime.substring(8, 10), 16).byteValue(),
				Integer.valueOf(tradingtime.substring(10, 12), 16).byteValue()}, new byte[0], new byte[0]);
		}
		
		return swipeResult;
	}
	
	
	private SwipResult getSwipResult(Swiper swiper, int trackKey,String encryptType) {
		SwipResult swipRslt = swiper.readEncryptResult(new SwiperReadModel[] {SwiperReadModel.READ_SECOND_TRACK,SwiperReadModel.READ_THIRD_TRACK },TrackSecurityPaddingType.NONE, new WorkingKey(trackKey),encryptType, null, null);
		return swipRslt;
	}

	@Override
	public DeviceInfo getDeviceInfo() {
		return deviceManager.getDevice().getDeviceInfo();
	}

	@Override
	public BatteryInfoResult getPowerLevel() {
		return deviceManager.getDevice().getBatteryInfo();
	}

	@Override
	public void reset() {
		deviceManager.getDevice().reset();
	}

	@Override
	public void destroy() {
		deviceManager.destroy();
	}

	public DeviceConnState getDeviceConnState() {
		return deviceManager.getDeviceConnState();
	}

	@Override
	public void setParam(int tag, byte[] value) {
		TLVPackage tlvpackage = ISOUtils.newTlvPackage();
		tlvpackage.append(tag, value);
		deviceManager.getDevice().setDeviceParams(tlvpackage);
	}

	@Override
	public byte[] getParam(int tag) {
		TLVPackage pack = deviceManager.getDevice().getDeviceParams(tag);
		return pack.getValue(getOrginTag(tag));

	}

	private int getOrginTag(int tag) {
		if ((tag & 0xFF0000) == 0xFF0000) {
			return tag & 0xFFFF;
		} else if ((tag & 0xFF00) == 0xFF00) {
			return tag & 0xFF;
		}
		return tag;
	}

	@Override
	public void printBitMap(int position, Bitmap bitmap) {
		Printer printer = (Printer) deviceManager.getDevice()
				.getStandardModule(ModuleType.COMMON_PRINTER);
		printer.init();
		printer.print(position, bitmap, 30, TimeUnit.SECONDS);
	}

	@Override
	public void printString(String data) {
		Printer printer = (Printer) deviceManager.getDevice()
				.getStandardModule(ModuleType.COMMON_PRINTER);
		printer.init();
		printer.print(data, 30, TimeUnit.SECONDS);
	}

	@Override
	public byte[] encrypt(WorkingKey wk, byte[] input) {
		PinInput pinInput = (PinInput) deviceManager.getDevice()
				.getStandardModule(ModuleType.COMMON_PININPUT);
		return pinInput.encrypt(wk, EncryptType.ECB, input, new byte[] { 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 });
	}

	@Override
	public byte[] caculateMac(byte[] input) {
		PinInput pinInput = (PinInput) deviceManager.getDevice()
				.getStandardModule(ModuleType.COMMON_PININPUT);
		return pinInput.calcMac(MacAlgorithm.MAC_ECB, new WorkingKey(
				MacWKIndexConst.DEFAULT_MAC_WK_INDEX), input);
	}

	@Override
	public String inputPlainPwd(String title, String content, int minLength,
			int maxLength, long timeout) throws InterruptedException {
		KeyBoard keyboard = (KeyBoard) deviceManager.getDevice()
				.getStandardModule(ModuleType.COMMON_KEYBOARD);
		EventHolder<KeyBoardReadingEvent<String>> listener = new EventHolder<KeyBoardReadingEvent<String>>();
		keyboard.readPwd(DispType.NORMAL, title, content, minLength, maxLength,
				(int) timeout, TimeUnit.SECONDS, listener);
		try {
			listener.startWait();
		} catch (InterruptedException e) {
			keyboard.cancelLastReading();
			throw e;
		} finally {
			// clearScreen();
		}
		KeyBoardReadingEvent<String> event = listener.event;
		if (event == null)
			return null;

		return event.getRslt();
	}

	/**
	 * 事件线程阻塞控制监听器.
	 * 
	 * @author lance
	 * 
	 * @param <T>
	 */
	private class EventHolder<T extends DeviceEvent> implements
			DeviceEventListener<T> {

		private T event;

		private final Object syncObj = new Object();

		private boolean isClosed = false;

		public void onEvent(T event, Handler handler) {
			this.event = event;
			synchronized (syncObj) {
				isClosed = true;
				syncObj.notify();
			}
		}

		public Handler getUIHandler() {
			return null;
		}

		void startWait() throws InterruptedException {
			synchronized (syncObj) {
				if (!isClosed)
					syncObj.wait();
			}
		}

	}

	@Override
	public void showMessageWithinTime(String msg, int showtime) {
		LCD lcd = (LCD) deviceManager.getDevice().getStandardModule(
				ModuleType.COMMON_LCD);
		if (lcd != null) {
			lcd.drawWithinTime(msg, showtime);
		}

	}

	@Override
	public SwipResult swipCardForPlain(String msg, long timeout,
			TimeUnit timeUnit) {
		CardReader cardReader = (CardReader) deviceManager.getDevice()
				.getStandardModule(ModuleType.COMMON_CARDREADER);
		if (cardReader == null) {
			throw new DeviceRTException(AppExCode.GET_TRACKTEXT_FAILED,
					"not support read card!");
		}
		try {
			EventHolder<OpenCardReaderEvent> listener = new EventHolder<OpenCardReaderEvent>();
			cardReader.openCardReader(msg,
					new ModuleType[] { ModuleType.COMMON_SWIPER }, timeout,
					timeUnit, listener);
			try {
				listener.startWait();
			} catch (InterruptedException e) {
				cardReader.cancelCardRead();
			} finally {
				// clearScreen();
			}
			OpenCardReaderEvent event = listener.event;
			event = preEvent(event, AppExCode.GET_TRACKTEXT_FAILED);
			if (event == null) {
				return null;
			}
			ModuleType[] openedModuleTypes = event.getOpenedCardReaders();
			if (openedModuleTypes == null || openedModuleTypes.length <= 0) {
				logger.info("start cardreader,but return is none!may user canceled?");
				return null;
			}
			if (openedModuleTypes.length > 1) {
				logger.warn("should return only one type of cardread action!but is "
						+ openedModuleTypes.length);
				throw new DeviceRTException(AppExCode.GET_TRACKTEXT_FAILED,
						"should return only one type of cardread action!but is "
								+ openedModuleTypes.length);
			}
			switch (openedModuleTypes[0]) {
			case COMMON_SWIPER:
				Swiper swiper = (Swiper) deviceManager.getDevice()
						.getStandardModule(ModuleType.COMMON_SWIPER);
				SwipResult swipRslt = swiper
						.readPlainResult(new SwiperReadModel[] {
								SwiperReadModel.READ_SECOND_TRACK,
								SwiperReadModel.READ_THIRD_TRACK });
				if (swipRslt.getRsltType() == SwipResultType.SUCCESS) {
					return swipRslt;
				}
				throw new DeviceRTException(AppExCode.GET_TRACKTEXT_FAILED,
						"swip failed:" + swipRslt.getRsltType());
			default: {
				throw new DeviceRTException(AppExCode.GET_TRACKTEXT_FAILED,
						"not support cardreader module:" + openedModuleTypes[0]);
			}
			}
		} finally {
			logger.info("closeCardReader2");
			// cardReader.closeCardReader(); // for me11
		}
	}

	private <T extends AbstractProcessDeviceEvent> T preEvent(T event,
			int defaultExCode) {
		if (!event.isSuccess()) {
			if (event.isUserCanceled()) {
				return null;
			}
			if (event.getException() != null) {
				if (event.getException() instanceof RuntimeException) {// 运行时异常直接抛出.
					throw (RuntimeException) event.getException();
				}
				throw new DeviceRTException(AppExCode.GET_TRACKTEXT_FAILED,
						"open card reader meet error!", event.getException());
			}
			throw new DeviceRTException(ExCode.UNKNOWN,
					"unknown exception!defaultExCode:" + defaultExCode);
		}
		return event;
	}

	@Override
	public void loadMainKey(KekUsingType kekUsingType, int mkIndex,
			byte[] keyData, byte[] checkValue, int transportKeyIndex) {
		PinInput pinInput = (PinInput) deviceManager.getDevice()
				.getStandardModule(ModuleType.COMMON_PININPUT);
		byte[] rslt = pinInput.loadMainKey(kekUsingType, mkIndex, keyData,
				transportKeyIndex);
		byte[] expectedKcv = new byte[4];
		System.arraycopy(rslt, 0, expectedKcv, 0, expectedKcv.length);
		if (!Arrays.equals(expectedKcv, checkValue)) {
			throw new RuntimeException("failed to check kcv!:["
					+ Dump.getHexDump(expectedKcv) + ","
					+ Dump.getHexDump(checkValue) + "]");
		}

	}

	@Override
	public String getCurrentDriverVersion() {
		if (deviceManager != null)
			return deviceManager.getDriverMajorVersion() + "."
					+ deviceManager.getDriverMinorVersion();

		return "n/a";
	}

	private void isConnected() {
		synchronized (this.driverName) {
			if (null == deviceManager || deviceManager.getDevice() == null) {
				throw new DeviceOutofLineException("device not connect!");
			}
		}
	}

	@Override
	public EmvModule getEmvModule() {
		isConnected();
		return (EmvModule) deviceManager.getDevice().getStandardModule(
				ModuleType.COMMON_EMV);
	}

	@Override
	public void startEmv(BigDecimal amt, TransferListener transferListener) {
		isConnected();
		try {
			EmvModule module = getEMVModule();
			EmvTransController controller = module
					.getEmvTransController(transferListener);
			controller.startEmv(amt, new BigDecimal("0"), true);
		} finally {
			logger.info("closeCardReader3");
			// cardReader.closeCardReader();
		}
	}

	private EmvModule getEMVModule() {
		return (EmvModule) deviceManager.getDevice().getStandardModule(
				ModuleType.COMMON_ME11EMV);
	}

	private QPBOCModule getQPBOCModule() {
		return (QPBOCModule) deviceManager.getDevice().getStandardModule(
				ModuleType.COMMON_QPBOC);
	}

	/* (non Javadoc) 
	 * @Title: getSwipType
	 * @Description: TODO
	 * @param msg
	 * @param timeout
	 * @param timeunit
	 * @return 
	 * @see com.apicloud.controller.DeviceController#getSwipType(java.lang.String, long, java.util.concurrent.TimeUnit) 
	 */
	@Override
	public ModuleType getSwipType(String msg, long timeout, TimeUnit timeunit) {
		    ModuleType type;
		    CardReader cardReader = (CardReader) deviceManager.getDevice().getStandardModule(ModuleType.COMMON_CARDREADER);
		    EventHolder<OpenCardReaderEvent> listener = new EventHolder<OpenCardReaderEvent>();
			cardReader.openCardReader(msg, new ModuleType[] { ModuleType.COMMON_SWIPER, ModuleType.COMMON_ICCARD }, timeout, timeunit, listener);
			try {
				listener.startWait();
			} catch (InterruptedException e) {
				cardReader.cancelCardRead();
			} finally {
				clearScreen();
			}
			OpenCardReaderEvent event = listener.event;
			//OpenCardReaderEvent event = listener.event;
			event = preEvent(event, AppExCode.GET_TRACKTEXT_FAILED);
			if (event == null) {
				throw new DeviceRTException(AppExCode.GET_TRACKTEXT_FAILED, "no event accept.");
			}
			ModuleType[] openedModuleTypes = event.getOpenedCardReaders();
			type=openedModuleTypes[0];
			
			return type;
	}

	/* (non Javadoc) 
	 * @Title: getTrackText
	 * @Description: TODO
	 * @param flag
	 * @return
	 * @throws InterruptedException 
	 * @see com.apicloud.controller.DeviceController#getTrackText(int) 
	 */
	@Override
	public SwipResult getTrackText(int flag) throws InterruptedException {
		int trackKey = DataEncryptWKIndexConst.DEFAULT_TRACK_WK_INDEX;
		Swiper swiper = (Swiper) deviceManager.getDevice().getStandardModule(ModuleType.COMMON_SWIPER);
		//SwipResult swipRslt = getSwipResult(swiper, trackKey, TrackEncryptAlgorithm.BY_UNIONPAY_MODEL, flag);
		SwipResult swipRslt = getSwipResult(swiper, trackKey, TrackEncryptAlgorithm.BY_UNIONPAY_MODEL, flag);
		if (swipRslt.getRsltType() == SwipResultType.SUCCESS) {
			return swipRslt;
		}
		throw new DeviceRTException(AppExCode.GET_TRACKTEXT_FAILED, "交易撤销");
	}

	/* (non Javadoc) 
	 * @Title: swipCardMe3X
	 * @Description: TODO
	 * @param msg
	 * @param strAmount
	 * @param transferListener
	 * @param timeout
	 * @param timeUnit
	 * @param handler
	 * @return 
	 * @see com.apicloud.controller.DeviceController#swipCardMe3X(java.lang.String, java.lang.String, com.apicloud.controller.TransferListener, long, java.util.concurrent.TimeUnit, android.os.Handler) 
	 */
	private long lastClickTime1 = 0;
	@Override
	public SwipResult swipCardMe3X(String msg, String strAmount, TransferListener transferListener, long timeout, TimeUnit timeUnit,boolean needTime) {
		Log.e("sean4", "------1");
		long currentTime = Calendar.getInstance().getTimeInMillis();
		SwipResult swipRslt = null;
		 if (currentTime - lastClickTime >MIN_CLICK_DELAY_TIME) {
			 lastClickTime = currentTime;
			 CardReader cardReader = (CardReader) deviceManager.getDevice()
						.getStandardModule(ModuleType.COMMON_CARDREADER);
				Log.e("sean4", "------2");
				if (cardReader == null) {
					throw new DeviceRTException(AppExCode.GET_TRACKTEXT_FAILED, "not support read card!");
				}
				
				//打开读卡器
				try {
					Log.e("sean4", "------3");
					EventHolder<OpenCardReaderEvent> listener = new EventHolder<OpenCardReaderEvent>();
					cardReader.openCardReader(msg, new ModuleType[] { ModuleType.COMMON_SWIPER, ModuleType.COMMON_ICCARD }, timeout
								, timeUnit, listener);

					Log.e("sean4", "------4");
					try {
						listener.startWait();
					} catch (InterruptedException e) {
						cardReader.cancelCardRead();
					} finally {
						clearScreen();
					}

					Log.e("sean4", "------5");
					OpenCardReaderEvent event = listener.event;
					event = preEvent(event, AppExCode.GET_TRACKTEXT_FAILED);
					if (event == null) {
						return null;
					}

					Log.e("sean4", "------6");
					ModuleType[] openedModuleTypes = event.getOpenedCardReaders();
					if (openedModuleTypes == null || openedModuleTypes.length <= 0) {
						logger.info("start cardreader,but return is none!may user canceled?");
						return null;
					}

					Log.e("sean4", "------7");
					if (openedModuleTypes.length > 1) {
						logger.warn("should return only one type of cardread action!but is " 
								+ openedModuleTypes.length);
						throw new DeviceRTException(AppExCode.GET_TRACKTEXT_FAILED, "should return only one type of cardread action!but is " + openedModuleTypes.length);
					}

					Log.e("sean4", "------8");
					//对不同模块进行不同的处理
					switch (openedModuleTypes[0]) {
						case COMMON_SWIPER:
						{
							Swiper swiper = (Swiper) deviceManager.getDevice()
												.getStandardModule(ModuleType.COMMON_SWIPER);
							Log.e("sean4", "------9");
		                   
		                             try {
		                            	 swipRslt=swiper.readPlainResult(new SwiperReadModel[] { 
		             							SwiperReadModel.READ_SECOND_TRACK, 
		             							SwiperReadModel.READ_THIRD_TRACK });
		                            	 if(swiper!=null){
		                            		 Log.e("sean11", "------notnull");
		                            	 }else{
		                            		 Log.e("sean11", "------null");
		                            	 }
									} catch (Exception e) {
										swipRslt=swiper.readPlainResult(new SwiperReadModel[] { 
		             							SwiperReadModel.READ_SECOND_TRACK, 
		             							SwiperReadModel.READ_THIRD_TRACK });
									}
		                    		if(swiper==null){
		                    			swipRslt=swiper.readPlainResult(new SwiperReadModel[] { 
		             							SwiperReadModel.READ_SECOND_TRACK, 
		             							SwiperReadModel.READ_THIRD_TRACK });
		                    		}
						
							Log.e("sean4", "------10");
							
								return swipRslt;
							
							
							//return swipRslt;
							
							//Log.e("sean4", "------11");
							//throw new DeviceRTException(AppExCode.GET_TRACKTEXT_FAILED, "swip failed:" + swipRslt.getRsltType());
						}
						
						case COMMON_ICCARD:
						{
							if(needTime){
								deviceManager.getDevice().setDeviceDate(new Date());
							}
							Log.v("", "");
							startEmvMe3X(msg, strAmount, timeout, transferListener);
							//return null;
						}
						
						default: 
						{
							
						  throw new DeviceRTException(AppExCode.GET_TRACKTEXT_FAILED, "not support cardreader module:" + openedModuleTypes[0]);
						}
					}
				}
				catch (Exception e) {
					Log.e("sean5", "------:"+e.getMessage());
					e.printStackTrace();
					return null;
				}
				finally {
					try {
						cardReader.closeCardReader();
					} catch (Exception e2) {
						// TODO: handle exception
					}
				}
			}
		 
		 return swipRslt;
		
	}

	/* (non Javadoc) 
	 * @Title: startEmvMe3X
	 * @Description: TODO
	 * @param msg
	 * @param strAmount
	 * @param timeout
	 * @param transferListener 
	 * @see com.apicloud.controller.DeviceController#startEmvMe3X(java.lang.String, java.lang.String, long, com.apicloud.controller.TransferListener) 
	 */
	@Override
	public void startEmvMe3X(String msg, String strAmount, long timeout, TransferListener transferListener) {
	    try {
	    	Log.v("", "");
			String strRealAmt;
			String strDisp;
			
			EmvModule emv = (EmvModule)deviceManager.getDevice().getStandardModule(ModuleType.COMMON_EMV);
			

			
			strRealAmt = strAmount;
			if(strAmount.length() <= 0){
				strRealAmt = "0";
			}
			
			
				//启动一个emv流程
				EmvTransController emvController = emv.getEmvTransController(transferListener);
				emvController.startEmv(0, 0x01, new BigDecimal(strRealAmt), true);
		} catch (Exception e) {
			Log.v("", "");
		}	
		
		
		
		
	}

	/* (non Javadoc) 
	 * @Title: getSwipResult3x
	 * @Description: TODO
	 * @return 
	 * @see com.apicloud.controller.DeviceController#getSwipResult3x() 
	 */
	@Override
	public SwipResult getSwipResult3x() {
		Swiper swiper = (Swiper) deviceManager.getDevice()
				.getStandardModule(ModuleType.COMMON_SWIPER);
 
       SwipResult swipRslt = swiper.readPlainResult(new SwiperReadModel[] { 
		SwiperReadModel.READ_SECOND_TRACK, 
		SwiperReadModel.READ_THIRD_TRACK });
       
       return swipRslt;
	}

	/* (non Javadoc) 
	 * @Title: swipCardMe3X
	 * @Description: TODO
	 * @param msg
	 * @param strAmount
	 * @param transferListener
	 * @param timeout
	 * @param timeUnit
	 * @param handler
	 * @return 
	 * @see com.apicloud.controller.DeviceController#swipCardMe3X(java.lang.String, java.lang.String, com.apicloud.controller.TransferListener, long, java.util.concurrent.TimeUnit, android.os.Handler) 
	 */
	 public static final int MIN_CLICK_DELAY_TIME = 1000;
     private long lastClickTime = 0;
	@Override
	public SwipResult swipCardMe3X(String msg, String strAmount, TransferListener transferListener, long timeout, TimeUnit timeUnit, Handler handler,boolean needTime) {
		long currentTime = Calendar.getInstance().getTimeInMillis();
		SwipResult swipRslt = null;
		 if (currentTime - lastClickTime >MIN_CLICK_DELAY_TIME) {
			 lastClickTime = currentTime;
			 CardReader cardReader = (CardReader) deviceManager.getDevice()
						.getStandardModule(ModuleType.COMMON_CARDREADER);
				Log.e("sean4", "------2");
				if (cardReader == null) {
					throw new DeviceRTException(AppExCode.GET_TRACKTEXT_FAILED, "not support read card!");
				}
				
				//打开读卡器
				try {
					Log.e("sean4", "------3");
					EventHolder<OpenCardReaderEvent> listener = new EventHolder<OpenCardReaderEvent>();
					cardReader.openCardReader(msg, new ModuleType[] { ModuleType.COMMON_SWIPER, ModuleType.COMMON_ICCARD }, timeout
								, timeUnit, listener);

					Log.e("sean4", "------4");
					try {
						listener.startWait();
					} catch (InterruptedException e) {
						cardReader.cancelCardRead();
					} finally {
						clearScreen();
					}

					Log.e("sean4", "------5");
					OpenCardReaderEvent event = listener.event;
					event = preEvent(event, AppExCode.GET_TRACKTEXT_FAILED);
					if (event == null) {
						return null;
					}

					Log.e("sean4", "------6");
					ModuleType[] openedModuleTypes = event.getOpenedCardReaders();
					if (openedModuleTypes == null || openedModuleTypes.length <= 0) {
						logger.info("start cardreader,but return is none!may user canceled?");
						return null;
					}

					Log.e("sean4", "------7");
					if (openedModuleTypes.length > 1) {
						logger.warn("should return only one type of cardread action!but is " 
								+ openedModuleTypes.length);
						throw new DeviceRTException(AppExCode.GET_TRACKTEXT_FAILED, "should return only one type of cardread action!but is " + openedModuleTypes.length);
					}

					Log.e("sean4", "------8");
					//对不同模块进行不同的处理
					switch (openedModuleTypes[0]) {
						case COMMON_SWIPER:
						{
							Swiper swiper = (Swiper) deviceManager.getDevice()
												.getStandardModule(ModuleType.COMMON_SWIPER);
							Log.e("sean4", "------9");
		                   
		                    try {
		                    	swipRslt=swiper.readPlainResult(new SwiperReadModel[] { 
		    							SwiperReadModel.READ_SECOND_TRACK, 
		    							SwiperReadModel.READ_THIRD_TRACK });
							} catch (Exception e) {
								swipRslt=swiper.readPlainResult(new SwiperReadModel[] { 
										SwiperReadModel.READ_SECOND_TRACK, 
										SwiperReadModel.READ_THIRD_TRACK });
							}
		                    		
						
							Log.e("sean4", "------10");
							if(swipRslt==null){
								Log.e("sean4", "------null");
								swipRslt=swiper.readPlainResult(new SwiperReadModel[] { 
										SwiperReadModel.READ_SECOND_TRACK, 
										SwiperReadModel.READ_THIRD_TRACK });
							}
							Message msg1=handler.obtainMessage();
							msg1.obj=swipRslt;
							handler.sendMessage(msg1);
							if (swipRslt.getRsltType() == SwipResultType.SUCCESS) {
								//return swipRslt;
							}
							
							
							
							//Log.e("sean4", "------11");
							//throw new DeviceRTException(AppExCode.GET_TRACKTEXT_FAILED, "swip failed:" + swipRslt.getRsltType());
						}
						
						case COMMON_ICCARD:
						{
							Message msg1=handler.obtainMessage();
							msg1.obj=null;
							handler.sendMessage(msg1);
							if(needTime){
								deviceManager.getDevice().setDeviceDate(new Date());
							}
							startEmvMe3X(msg, strAmount, timeout, transferListener);
							//return null;
							
						}
						
						default: 
						{
							throw new DeviceRTException(AppExCode.GET_TRACKTEXT_FAILED, "not support cardreader module:" + openedModuleTypes[0]);
						}
					}
				}
				catch (Exception e) {
					Log.e("seanerror", "------:"+e.getMessage());
					e.printStackTrace();
					return null;
				}
				finally {
					try {
						cardReader.closeCardReader();
					} catch (Exception e2) {
						// TODO: handle exception
					}
				}
			 
		 }
		return swipRslt;
			 
		 
			 
		 
		
	}

	/* (non Javadoc) 
	 * @Title: startReadingPwd
	 * @Description: TODO
	 * @param msg
	 * @param transferListener 
	 * @see com.apicloud.controller.DeviceController#startReadingPwd(java.lang.String, com.apicloud.controller.TransferListener) 
	 */
	@Override
	public void startReadingPwd(String msg, DeviceListener lsnr) {
		KeyBoard kb = (KeyBoard) deviceManager.getDevice().getStandardModule(ModuleType.COMMON_KEYBOARD);
		kb.readPwd(com.newland.mtype.module.common.lcd.DispType.NORMAL,"",msg,5,50,30,TimeUnit.SECONDS, lsnr);
		
		while (lsnr.pwdInputFinish == false) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				Log.e("startReadingPwd:  ", e.getMessage());
			}
		}
		lsnr.pwdInputFinish = true;
		return;

		
	}
	
	/**
	 * 随机生成16位数加密
	 * 
	 */
	
	 public String getHex_workkey(){
		String workkey="";
		for(int i=0;i<16;i++){
		workkey+=Integer.toHexString(new Random().nextInt(16));
		}
		return workkey.toUpperCase();
		}

	/* (non Javadoc) 
	 * @Title: swipCommonCard
	 * @Description: TODO
	 * @param msg
	 * @param timeout
	 * @param timeUnit
	 * @return 
	 * @see com.apicloud.controller.DeviceController#swipCommonCard(java.lang.String, long, java.util.concurrent.TimeUnit) 
	 */
	

	/* (non Javadoc) 
	 * @Title: swipBlueToothCard
	 * @Description: TODO
	 * @param msg
	 * @param timeout
	 * @param timeUnit
	 * @return 
	 * @see com.apicloud.controller.DeviceController#swipBlueToothCard(java.lang.String, long, java.util.concurrent.TimeUnit) 
	 */
	
}
