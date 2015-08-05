DESCRIPTION = "Package containing Makefile and Rules.make file for TISDKs"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

# Build the list of component makefiles to put together to build the
# Makefile that goes into the SDK.  For legacy devices the base Makefile
# will be picked up and will contain everything.
#
# It is assumed that the component makefiles follow the naming
# Makefile_$component.  All Makefiles will be part of the SRC_URI to be
# fetched, but only the listed ones will be used to build the final Makefile

SRC_URI = "\
    file://Makefile \
    file://Rules.make \
    file://Makefile_linux \
    file://Makefile_linux-dtbs \
    file://Makefile_u-boot-legacy \
    file://Makefile_matrix-gui \
    file://Makefile_arm-benchmarks \
    file://Makefile_ti-crypto-examples \
    file://Makefile_am-sysinfo \
    file://Makefile_av-examples \
    file://Makefile_u-boot-spl \
    file://Makefile_matrix-gui-browser \
    file://Makefile_refresh-screen \
    file://Makefile_pru \
    file://Makefile_ti-ocf-crypto-module \
    file://Makefile_qt-tstat \
    file://Makefile_quick-playground \
    file://Makefile_wireless \
    file://Makefile_omapconf \
    file://Makefile_oprofile-example \
    file://Makefile_dual-camera-demo \
    file://Makefile_image-gallery \
    file://Makefile_cryptodev \
    file://Makefile_sgx-modules \
    file://Makefile_cmem-mod \
    file://Makefile_debugss-module-drv \
    file://Makefile_gdbserverproxy-module-drv \
"

PR = "r49"

MAKEFILES_COMMON = "linux \
                    matrix-gui \
                    arm-benchmarks \
                    am-sysinfo \
                    matrix-gui-browser \
                    refresh-screen \
                    ${@base_conditional('QT_PROVIDER', 'qt5', '', 'qt-tstat', d)} \
                    oprofile-example \
"
MAKEFILES = ""

# This example application should not be used when using non-SGX
QUICK_PLAYGROUND = "${@base_conditional('ARAGO_QT_PROVIDER','qt4-embedded-gles','quick-playground','', d)}"

# Add device specific make targets

MAKEFILES_append_omap3 = " u-boot-spl \
                           ${QUICK_PLAYGROUND} \
"
MAKEFILES_append_am37x-evm = " av-examples \
                               ti-ocf-crypto-module \
"
MAKEFILES_append_am3517-evm = " av-examples \
                                ti-ocf-crypto-module \
"
MAKEFILES_append_ti33x = " u-boot-spl \
                           ${QUICK_PLAYGROUND} \
                           ti-crypto-examples \
                           linux-dtbs \
                           wireless \
                           cryptodev \
                           sgx-modules \
"
MAKEFILES_append_ti43x = " u-boot-spl \
                           ${QUICK_PLAYGROUND} \
                           ti-crypto-examples \
                           linux-dtbs \
                           wireless \
                           cryptodev \
                           sgx-modules \
                           dual-camera-demo \
                           image-gallery \
"

MAKEFILES_append_dra7xx = " cryptodev \
                            debugss-module-drv \
                            gdbserverproxy-module-drv \
"

MAKEFILES_append_omap-a15 = " u-boot-spl \
                              ${QUICK_PLAYGROUND} \
                              omapconf \
                              linux-dtbs \
                              cmem-mod \
"
MAKEFILES_append_am180x-evm = " pru \
                                u-boot-legacy \
"

# Use ARCH format expected by the makefile
PLATFORM_ARCH = "armv7-a"
PLATFORM_ARCH_omapl138 = "armv5te"

PLATFORM_SGX = ""
PLATFORM_SGX_ti33x = "ti335x"
PLATFORM_SGX_ti43x = "ti43xx"

PLATFORM_DEBUGSS = ""
PLATFORM_DEBUGSS_dra7xx = "DRA7xx_PLATFORM"

PLATFORM_GDBSERVERPROXY = ""
PLATFORM_GDBSERVERPROXY_dra7xx = "DRA7xx_PLATFORM"

KERNEL_BUILD_CMDS = "${@base_conditional('KERNEL_IMAGETYPE','uImage','LOADADDR=${UBOOT_LOADADDRESS} uImage','zImage',d)}"

KERNEL_DEVICETREE_ti33x = "am335x-evm.dtb am335x-evmsk.dtb am335x-bone.dtb am335x-boneblack.dtb"
KERNEL_DEVICETREE_ti43x = "am43x-epos-evm.dtb am437x-gp-evm.dtb am437x-sk-evm.dtb"
KERNEL_DEVICETREE_beaglebone = "am335x-bone.dtb am335x-boneblack.dtb"
KERNEL_DEVICETREE_omap5-evm = "omap5-uevm.dtb"
KERNEL_DEVICETREE_dra7xx = "dra7-evm.dtb dra72-evm.dtb am57xx-beagle-x15.dtb am57xx-evm.dtb"

DEFCONFIG = "tisdk_${MACHINE}_defconfig"

AMSDK_DEFCONFIG = "singlecore-omap2plus_defconfig"

DEFCONFIG := "${@base_conditional('ARAGO_BRAND','amsdk','${AMSDK_DEFCONFIG}','${DEFCONFIG}',d)}"

# This step will stitch together the final Makefile based on the supported
# make targets.
do_install () {
    install -d ${D}/

    # Start with the base Makefile
    install  ${WORKDIR}/Makefile ${D}/Makefile

    # Remember the targets added so we can update the all target
    targets=""
    clean_targets=""
    install_targets=""

    # Now add common build targets
    for x in ${MAKEFILES_COMMON}
    do
        cat ${WORKDIR}/Makefile_${x} >> ${D}/Makefile
        targets="$targets""$x\ "
        clean_targets="$clean_targets""$x""_clean\ "
        install_targets="$install_targets""$x""_install\ "
    done

    # Now add device specific build targets
    for x in ${MAKEFILES}
    do
        cat ${WORKDIR}/Makefile_${x} >> ${D}/Makefile
        targets="$targets""$x\ "
        clean_targets="$clean_targets""$x""_clean\ "
        install_targets="$install_targets""$x""_install\ "
    done

    # Update the all, clean, and install targets if we added targets
    if [ "$targets" != "" ]
    then
        sed -i -e "s/__ALL_TARGETS__/$targets/" ${D}/Makefile
        sed -i -e "s/__CLEAN_TARGETS__/$clean_targets/" ${D}/Makefile
        sed -i -e "s/__INSTALL_TARGETS__/$install_targets/" ${D}/Makefile
    fi

    sed -i -e "s/__KERNEL_BUILD_CMDS__/${KERNEL_BUILD_CMDS}/" ${D}/Makefile

    sed -i -e "s/__PLATFORM_SGX__/${PLATFORM_SGX}/" ${D}/Makefile
    sed -i -e "s/__PLATFORM_DEBUGSS__/${PLATFORM_DEBUGSS}/g" ${D}/Makefile
    sed -i -e "s/__PLATFORM_GDBSERVERPROXY__/${PLATFORM_GDBSERVERPROXY}/g" ${D}/Makefile

    cat ${D}/Makefile | grep "__DTB_DEPEND__" > /dev/null

    if [ "$?" == "0" ]
    then
        sed -i -e "s/__KERNEL_DEVICETREE__/${KERNEL_DEVICETREE}/" ${D}/Makefile
        sed -i -e "s/__DTB_DEPEND__/linux-dtbs/" ${D}/Makefile
        sed -i -e "s/__DTB_DEPEND_INSTALL__/linux-dtbs_install/" ${D}/Makefile
    else
        sed -i -e "s/__DTB_DEPEND__//" ${D}/Makefile
        sed -i -e "s/__DTB_DEPEND_INSTALL__//" ${D}/Makefile
    fi


    install  ${WORKDIR}/Rules.make ${D}/Rules.make

    # fixup Rules.make values
    sed -i -e "s/__PLATFORM__/${MACHINE}/" ${D}/Rules.make
    sed -i -e "s/__DEFCONFIG__/${DEFCONFIG}/" ${D}/Rules.make
    sed -i -e "s/__ARCH__/${PLATFORM_ARCH}/" ${D}/Rules.make
    sed -i -e "s/__TOOLCHAIN_PREFIX__/${TOOLCHAIN_SYS}-/" ${D}/Rules.make
    sed -i -e "s/__UBOOT_MACHINE__/${UBOOT_MACHINE}/" ${D}/Rules.make
    sed -i -e "s/__CFLAGS__/${TARGET_CC_ARCH}/" ${D}/Rules.make

}

PACKAGE_ARCH = "${MACHINE_ARCH}"

FILES_${PN} = "/*"
