DESCRIPTION = "Firmware images to test IPC"
LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/BSD;md5=3775480a712fc46a69647678acb234cb"

PACKAGE_ARCH = "${MACHINE_ARCH}"

FWVER = "eng-4-gb9283b0"
PR = "r0+${FWVER}"

COMPATIBLE_MACHINE = "omap5-evm|dra7xx"

INHIBIT_PACKAGE_STRIP = "1"
INSANE_SKIP_${PN} = "arch"

S = "${WORKDIR}/${PV}_${FWVER}"

SRC_URI = "http://arago-project.org/files/releases/ipc-test-fw/${PV}_${FWVER}.tar.gz"

SRC_URI[md5sum] = "4fcba6844949ea909131684a80233800"
SRC_URI[sha256sum] = "3b90b195c382937551251055aaa9185627e86027c91fb41a594dc390391d5fb6"

FW_FILES_omap5-evm = "tesla-dsp.xe64T ducati-m3-core0.xem3"
FW_FILES_dra7xx = "dra7-dsp1-fw.xe66 dra7-dsp2-fw.xe66 dra7-ipu1-fw.xem4 dra7-ipu2-fw.xem4"

do_compile() {
    :
}

# Add a .test extension to the firmware files so that they do not conflict
# with the real firmware images
do_install() {
    install -d ${D}${base_libdir}/firmware
    for f in ${FW_FILES}
    do
        install -m 755 ${f} ${D}${base_libdir}/firmware/${f}.test
    done
}

FILES_${PN} += "${base_libdir}/firmware"
