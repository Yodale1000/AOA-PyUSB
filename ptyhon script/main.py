import threading
import time

import usb

from attrs import *

ACCESSORY_VID = 0x18D1
ACCESSORY_PID = (0x2D00, 0x2D01, 0x2D04, 0x2D05)


def main():
    while True:
        accessory_task(ACCESSORY_VID)
        print("accessory task finished")


def accessory_task(vid):
    dev = usb.core.find(idVendor=vid)

    if dev is None:
        raise ValueError("No compatible device not found")

    print("compatible device found")

    if dev.idProduct in ACCESSORY_PID:
        print("device is in accessory mode")
    else:
        print("device is not in accessory mode yet,  VID %04X" % vid)

        accessory(dev)

        if dev is None:
            raise ValueError("No compatible device not found")

    cfg = dev.get_active_configuration()
    if_num = cfg[(0, 0)].bInterfaceNumber
    intf = usb.util.find_descriptor(cfg, bInterfaceNumber=if_num)

    ep_out = usb.util.find_descriptor(
        intf,
        custom_match= \
            lambda e: \
                usb.util.endpoint_direction(e.bEndpointAddress) == \
                usb.util.ENDPOINT_OUT
    )

    ep_in = usb.util.find_descriptor(
        intf,
        custom_match= \
            lambda e: \
                usb.util.endpoint_direction(e.bEndpointAddress) == \
                usb.util.ENDPOINT_IN
    )

    writer_thread = threading.Thread(target=writer, args=(ep_out,))
    writer_thread.start()

    while True:
        try:
            data = ep_in.read(size_or_buffer=1, timeout=0)
            print("read value %d" % data[0])
        except usb.core.USBError as e:
            print("failed to send IN transfer")
            print(e)
            break

    writer_thread.join()
    print("exiting application")


def writer(ep_out):
    while True:
        try:
            length = ep_out.write([0], timeout=0)
            print("%d bytes written" % length)
            time.sleep(0.5)

        except usb.core.USBError:
            print("error in writer thread")
            break


def accessory(dev):
    protocol_version = int.from_bytes(dev.ctrl_transfer(usb.util.CTRL_TYPE_VENDOR | usb.util.CTRL_IN, 51, 0, 0, 2),
                                      byteorder='little')
    print(f"version is: {protocol_version}")

    assert dev.ctrl_transfer(
        usb.util.CTRL_TYPE_VENDOR | usb.util.CTRL_OUT,
        52, 0, 0, MANUFACTURER) == len(MANUFACTURER)

    assert dev.ctrl_transfer(
        usb.util.CTRL_TYPE_VENDOR | usb.util.CTRL_OUT,
        52, 0, 1, MODEL_NAME) == len(MODEL_NAME)

    assert dev.ctrl_transfer(
        usb.util.CTRL_TYPE_VENDOR | usb.util.CTRL_OUT,
        52, 0, 2, DESCRIPTION) == len(DESCRIPTION)

    assert dev.ctrl_transfer(
        usb.util.CTRL_TYPE_VENDOR | usb.util.CTRL_OUT,
        52, 0, 3, VERSION) == len(VERSION)

    assert dev.ctrl_transfer(
        usb.util.CTRL_TYPE_VENDOR | usb.util.CTRL_OUT,
        52, 0, 4, URL) == len(URL)

    assert dev.ctrl_transfer(
        usb.util.CTRL_TYPE_VENDOR | usb.util.CTRL_OUT,
        52, 0, 5, SERIAL_NUMBER) == len(SERIAL_NUMBER)

    dev.ctrl_transfer(
        usb.util.CTRL_TYPE_VENDOR | usb.util.CTRL_OUT,
        53, 0, 0, None)

    time.sleep(1)


if __name__ == "__main__":
    main()
