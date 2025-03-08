package com.edgetech.bbscout.features.capture.presentation.review_data

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.navOptions
import com.diracks.app.app.app_state.BBScoutAppState
import com.edgetech.bbscout.components.ui.LargeDropdownMenu
import com.edgetech.bbscout.components.utils.ifEmptySetNull
import com.edgetech.bbscout.features.capture.domain.model.BillboardExtractedInfo
import com.edgetech.bbscout.features.capture.domain.model.BillboardTypeErrorException
import com.edgetech.bbscout.features.capture.domain.model.BrandDescriptionErrorException
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordEventSink
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiEvent
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiModel
import com.edgetech.bbscout.features.capture.domain.viewmodel.CaptureRecordViewmodel
import com.edgetech.bbscout.features.capture.presentation.capture_detail.SubTitleComp
import com.edgetech.bbscout.features.capture.presentation.capture_flow.select_billboard_type.BillboardSideCountComp
import com.edgetech.bbscout.features.navigation.AppDestinations
import com.example.core.core.utils.components.LocationAwareActivity
import kotlinx.coroutines.launch


@Composable
fun EditRecordScreen(
    appState: BBScoutAppState?,
    captureRecordViewmodel: CaptureRecordViewmodel,
    recordId: Long?,
    recordType: RecordType
) {
    EditRecordMain(
        appState = appState,
        captureRecordUiModel = captureRecordViewmodel.uiModel,
        recordId = recordId,
        recordType
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecordMain(
    appState: BBScoutAppState?,
    captureRecordUiModel: CaptureRecordUiModel,
    recordId: Long?,
    recordType: RecordType
) {

    val context = LocalActivity.current as LocationAwareActivity

    val captureRecordUiState by captureRecordUiModel.captureUiState.collectAsState()
    val currentData = getActiveBillboardData(captureRecordUiState)

    val captureRecordUiEvent by captureRecordUiModel.captureUiEvent.collectAsState()


    var campaignBrand = rememberTextFieldState()
    var campaignDescription = rememberTextFieldState()


    var billboardType by rememberSaveable {
        mutableStateOf("")
    }

    var billboardOwner = rememberTextFieldState()

    var billboardWidth = rememberTextFieldState()

    var billboardLength = rememberTextFieldState()

    var ownerWebsite = rememberTextFieldState()

    val billboardTypes = listOf(
        "Static Billboard",
        "Digital Billboard",
        "Banner Ads",
        "Wallscapes",
        "Mobile Billboards",
        "Lamp Posts",
        "Interactive Billboards"
    )

    val unitOfMeasurements = listOf("centimeters", "meters", "feet", "inches")

    var selectedUnitOfMeasurement by rememberSaveable {
        mutableStateOf("")
    }

    val structureTypes = listOf(
        "Bridge",
        "digital",
        "free standing",
        "Gantry",
        "hoarding",
        "Hooding",
        "Right",
        "Sky",
        "sky sign",
        "wall wrap"
    )

    val materialTypes = listOf(
        "backlit", "digital", "flex", "LED", "Vinyl", "Sticker", "Metal", "Mesh"
    )

    val angleType = listOf(
        "double decker", "Head On", "Left", "Right"
    )

    val visibilityTypes = listOf(
        "Average", "Excellent", "Good", "Poor"
    )

    val illuminationTypes = listOf(
        "front", "none"
    )

    var selectedStructureType by rememberSaveable {
        mutableStateOf("")
    }

    var selectedMaterialType by rememberSaveable {
        mutableStateOf("")
    }

    var selectedAngleType by rememberSaveable {
        mutableStateOf("")
    }

    var selectedVisibilityType by rememberSaveable {
        mutableStateOf("")
    }

    var selectedIlluminationType by rememberSaveable {
        mutableStateOf("")
    }


    var targetAge = rememberTextFieldState()

    var targetGender = rememberTextFieldState()

    var phoneNumbers by remember {
        mutableStateOf(listOf<Long>())
    }

    var emails by remember {
        mutableStateOf(listOf<String>())
    }

    var websites by remember {
        mutableStateOf(listOf<String>())
    }

    var socialMedias by remember {
        mutableStateOf(listOf<String>())
    }

    var products by remember {
        mutableStateOf(listOf<String>())
    }

    var ownerPhone by remember {
        mutableStateOf(listOf<Long>())
    }

    var ownerEmail by remember {
        mutableStateOf(listOf<String>())
    }

    var selectedEditTypeList by rememberSaveable {
        mutableStateOf<RecordTypeList?>(null)
    }

    var selectedEditValue by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var isOccupied by rememberSaveable {
        mutableStateOf<Boolean?>(null)
    }

    if (selectedEditTypeList != null) {
        Dialog(onDismissRequest = {
            selectedEditTypeList = null
            selectedEditValue = null
        }) {
            val dialogValue = rememberTextFieldState()
            LaunchedEffect(selectedEditValue) {
                dialogValue.setTextAndPlaceCursorAtEnd(
                    selectedEditValue ?: ""
                )
            }
            val title = when (selectedEditTypeList) {
                RecordTypeList.PHONE -> "Phone number"
                RecordTypeList.EMAIL -> "Email"
                RecordTypeList.WEBSITE -> "Website"
                RecordTypeList.SOCIAL_MEDIA -> "Social media"
                RecordTypeList.PRODUCT -> "Product"
                null -> {
                    ""
                }

                RecordTypeList.OWNNER_PHONE ->
                    "Phone number"

                RecordTypeList.OWNNER_EMAIL -> "Email"
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = title, style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp),
                    )

                    ExtractedInfoEditText(
                        keyboardType = if (selectedEditTypeList == RecordTypeList.PHONE) KeyboardType.Phone else KeyboardType.Text,
                        textState = dialogValue,
                    )
                    Button(
                        onClick = {
                            when (selectedEditTypeList) {
                                RecordTypeList.PHONE -> {
                                    if (phoneNumbers.contains(selectedEditValue?.toLongOrNull()))
                                        phoneNumbers =
                                            phoneNumbers.minus(selectedEditValue!!.toLong())
                                    if (dialogValue.text.toString().toLongOrNull() != null)
                                        phoneNumbers =
                                            phoneNumbers.plus(dialogValue.text.toString().toLong())
                                    selectedEditTypeList = null
                                    selectedEditValue = null

                                }

                                RecordTypeList.OWNNER_PHONE -> {
                                    if (ownerPhone.contains(selectedEditValue?.toLongOrNull()))
                                        ownerPhone =
                                            ownerPhone.minus(selectedEditValue!!.toLong())
                                    if (dialogValue.text.toString().toLongOrNull() != null)
                                        ownerPhone =
                                            ownerPhone.plus(dialogValue.text.toString().toLong())
                                    selectedEditTypeList = null
                                    selectedEditValue = null

                                }

                                RecordTypeList.OWNNER_EMAIL -> {
                                    if (ownerEmail.contains(selectedEditValue))
                                        ownerEmail = ownerEmail.minus(selectedEditValue!!)
                                    if (dialogValue.text.toString().isNotEmpty())
                                        ownerEmail = ownerEmail.plus(dialogValue.text.toString())
                                    selectedEditTypeList = null
                                    selectedEditValue = null
                                }

                                RecordTypeList.EMAIL -> {
                                    if (emails.contains(selectedEditValue))
                                        emails = emails.minus(selectedEditValue!!)
                                    if (dialogValue.text.toString().isNotEmpty())
                                        emails = emails.plus(dialogValue.text.toString())
                                    selectedEditTypeList = null
                                    selectedEditValue = null
                                }

                                RecordTypeList.WEBSITE -> {
                                    if (websites.contains(selectedEditValue))
                                        websites = websites.minus(selectedEditValue!!)
                                    if (dialogValue.text.toString().isNotEmpty())
                                        websites = websites.plus(dialogValue.text.toString())
                                    selectedEditTypeList = null
                                    selectedEditValue = null
                                }

                                RecordTypeList.SOCIAL_MEDIA -> {
                                    if (socialMedias.contains(selectedEditValue))
                                        socialMedias = socialMedias.minus(selectedEditValue!!)
                                    if (dialogValue.text.toString().isNotEmpty()) {
                                        socialMedias =
                                            socialMedias.plus(dialogValue.text.toString())
                                    }
                                    selectedEditTypeList = null
                                    selectedEditValue = null
                                }

                                RecordTypeList.PRODUCT -> {
                                    if (products.contains(selectedEditValue))
                                        products = products.minus(selectedEditValue!!)
                                    if (dialogValue.text.toString().isNotEmpty())
                                        products = products.plus(dialogValue.text.toString())
                                    selectedEditTypeList = null
                                    selectedEditValue = null
                                }

                                null -> {
                                    selectedEditTypeList = null
                                    selectedEditValue = null
                                }
                            }
                        },
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Text("Save")
                    }

                }
            }
        }

    }
    LaunchedEffect(
        recordId
    ) {
        if (recordId == null) {
            campaignBrand.setTextAndPlaceCursorAtEnd(currentData?.brandName?.ifEmptySetNull() ?: "")
            campaignDescription.setTextAndPlaceCursorAtEnd(
                currentData?.brandCampaign.ifEmptySetNull() ?: ""
            )
            billboardType = currentData?.billboardType.ifEmptySetNull() ?: ""
            billboardOwner.setTextAndPlaceCursorAtEnd(
                currentData?.billboardOwner.ifEmptySetNull() ?: ""
            )
            billboardWidth.setTextAndPlaceCursorAtEnd(
                currentData?.billboardWidth.ifEmptySetNull() ?: ""
            )
            billboardLength.setTextAndPlaceCursorAtEnd(
                currentData?.billboardLength.ifEmptySetNull() ?: ""
            )

            targetGender.setTextAndPlaceCursorAtEnd(
                currentData?.targetGender.ifEmptySetNull() ?: ""
            )

            targetAge.setTextAndPlaceCursorAtEnd(
                currentData?.targetAge.ifEmptySetNull() ?: ""
            )

            ownerWebsite.setTextAndPlaceCursorAtEnd(
                currentData?.ownerWebsite.ifEmptySetNull() ?: ""
            )

            isOccupied = currentData?.isOccupied
            selectedStructureType = currentData?.structure ?: ""
            selectedMaterialType = currentData?.material ?: ""
            selectedAngleType = currentData?.angle ?: ""
            selectedVisibilityType = currentData?.visibility ?: ""
            selectedIlluminationType = currentData?.illumination ?: ""

            selectedUnitOfMeasurement = currentData?.unitOfMeasurement ?: ""

            phoneNumbers = currentData?.phone ?: listOf()
            emails = currentData?.email ?: listOf()
            websites = currentData?.siteUrl ?: listOf()
            socialMedias = currentData?.campainSocials ?: listOf()
            products = currentData?.products ?: listOf()
            ownerPhone = currentData?.ownerContacts ?: listOf()
            ownerEmail = currentData?.ownerEmail ?: listOf()
        }
    }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    if (captureRecordUiEvent is CaptureRecordUiEvent.CaptureRecordCreated) {

        appState?.navController?.navigate(AppDestinations.BillboardAdded, navOptions = navOptions {
            popUpTo(AppDestinations.Dashboard)
        })
        captureRecordUiModel.captureEventSink(
            CaptureRecordEventSink.ResetUiEvent
        )
    } else if (captureRecordUiEvent is CaptureRecordUiEvent.Error) {
        val request = (captureRecordUiEvent as CaptureRecordUiEvent.Error)
        if (request.exception is BrandDescriptionErrorException) {

            LaunchedEffect(
                true
            ) {
                scope.launch {
                    val result = snackbarHostState
                        .showSnackbar(
                            message = "Please provide a brand description",
                            duration = SnackbarDuration.Short
                        )
                    when (result) {
                        SnackbarResult.Dismissed -> {
                            captureRecordUiModel.captureEventSink(
                                CaptureRecordEventSink.ResetUiEvent
                            )
                        }

                        else -> {}
                    }
                }
            }
        } else if (request.exception is BillboardTypeErrorException) {
            LaunchedEffect(
                true
            ) {
                scope.launch {
                    val result = snackbarHostState
                        .showSnackbar(
                            message = "Please provide a billboard type",
                            duration = SnackbarDuration.Short
                        )
                    when (result) {
                        SnackbarResult.Dismissed -> {
                            captureRecordUiModel.captureEventSink(
                                CaptureRecordEventSink.ResetUiEvent
                            )
                        }

                        else -> {}
                    }
                }
            }
        }
    }


    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
                    .copy(containerColor = MaterialTheme.colorScheme.background),
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text(text = "Billboard") },
                navigationIcon = {
                    IconButton(onClick = {
                        appState?.navController?.navigateUp()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Navigate up",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },

                )
        },

        ) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {

                if (recordType == RecordType.CAMPAIGN) {

                    Text(
                        text = "Campaign information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    SubTitleComp(
                        "Brand name",
                    )
                    ExtractedInfoEditText(
                        textState = campaignBrand,
                        placeholder = "Campaign brand"
                    )
                    SubTitleComp(
                        "Campaign description",

                        )
                    ExtractedInfoEditText(
                        textState = campaignDescription,
                        placeholder = "Campaign description"
                    )

                    SubTitleComp(
                        "Target gender",

                        )
                    ExtractedInfoEditText(
                        textState = targetGender,
                        placeholder = "Target gender"
                    )

                    SubTitleComp(
                        "Target age",

                        )
                    ExtractedInfoEditText(
                        keyboardType = KeyboardType.Number,
                        textState = targetAge,
                        placeholder = "Target age"
                    )
                    GroupHeader(title = "Products", onIconClick = {
                        selectedEditTypeList = RecordTypeList.PRODUCT
                        selectedEditValue = null
                    })
                    products.forEach {
                        ListItemComp(title = it,
                            onIconClick = {
                                selectedEditTypeList = RecordTypeList.PRODUCT
                                selectedEditValue = it
                            }, onDeletClick = {
                                products = products.minus(it)
                            })
                    }
                }
                if (recordType == RecordType.BILLBOARD_STRUCTURE) {
                    Text(
                        text = "Billboard structure",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    SubTitleComp(
                        "Billboard measurement",

                        )
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 2.dp)
                        ) {
                            SubTitleComp(
                                "Width",

                                )
                            ExtractedInfoEditText(
                                keyboardType = KeyboardType.Number,
                                textState = billboardWidth,
                                placeholder = "Width"
                            )

                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 2.dp)
                        ) {
                            SubTitleComp(
                                "Height",
                            )
                            ExtractedInfoEditText(
                                keyboardType = KeyboardType.Number,
                                textState = billboardLength,
                                placeholder = "Height"
                            )
                        }
                    }
                    LargeDropdownMenu(
                        modifier = Modifier
                            .fillMaxWidth(),
                        label = "Unit of measurement",
                        items = unitOfMeasurements,
                        selectedIndex = unitOfMeasurements.indexOf(selectedUnitOfMeasurement),
                        onItemSelected = { index, item ->
                            selectedUnitOfMeasurement = item
                        },
                    )
                    LargeDropdownMenu(
                        modifier = Modifier
                            .fillMaxWidth(),
                        label = "Structure",
                        items = structureTypes,
                        selectedIndex = structureTypes.indexOf(selectedStructureType),
                        onItemSelected = { index, item ->
                            selectedStructureType = item
                        },
                    )

                    LargeDropdownMenu(
                        modifier = Modifier
                            .fillMaxWidth(),
                        label = "Material",
                        items = materialTypes,
                        selectedIndex = materialTypes.indexOf(selectedMaterialType),
                        onItemSelected = { index, item ->
                            selectedMaterialType = item
                        },
                    )

                    LargeDropdownMenu(
                        modifier = Modifier
                            .fillMaxWidth(),
                        label = "Illumination",
                        items = illuminationTypes,
                        selectedIndex = illuminationTypes.indexOf(selectedIlluminationType),
                        onItemSelected = { index, item ->
                            selectedIlluminationType = item
                        },
                    )

                    LargeDropdownMenu(
                        modifier = Modifier
                            .fillMaxWidth(),
                        label = "Visibility",
                        items = visibilityTypes,
                        selectedIndex = visibilityTypes.indexOf(selectedVisibilityType),
                        onItemSelected = { index, item ->
                            selectedVisibilityType = item
                        },
                    )

                    LargeDropdownMenu(
                        modifier = Modifier
                            .fillMaxWidth(),
                        label = "Angle",
                        items = angleType,
                        selectedIndex = angleType.indexOf(selectedAngleType),
                        onItemSelected = { index, item ->
                            selectedAngleType = item
                        },
                    )

                }
                if (recordType == RecordType.BILLBOARD_INFO) {
                    Text(
                        text = "Billboard information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    SubTitleComp(
                        "Billboard owner",

                        )
                    ExtractedInfoEditText(
                        textState = billboardOwner,
                        placeholder = "Billboard owner"
                    )
                    SubTitleComp(
                        "Is occupied",
                    )
                    Row {
                        BillboardSideCountComp(
                            "Yes",
                            isOccupied == true,
                            onClick = {
                                isOccupied = true
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        BillboardSideCountComp(
                            "No",
                            isOccupied == false,
                            onClick = {
                                isOccupied = false
                            }
                        )

                    }
                    LargeDropdownMenu(
                        modifier = Modifier
                            .fillMaxWidth(),
                        label = "Billboard type",
                        items = billboardTypes,
                        selectedIndex = billboardTypes.indexOf(billboardType),
                        onItemSelected = { index, item ->
                            billboardType = item
                        },
                    )
                    SubTitleComp(
                        "Owner website",
                    )
                    ExtractedInfoEditText(
                        textState = ownerWebsite,
                        placeholder = "Owner website"
                    )
                    GroupHeader(title = "Phone numbers", onIconClick = {
                        selectedEditTypeList = RecordTypeList.OWNNER_PHONE
                        selectedEditValue = null
                    })
                    ownerPhone.forEach {
                        ListItemComp(title = it.toString(), onIconClick = {
                            selectedEditTypeList = RecordTypeList.OWNNER_PHONE
                            selectedEditValue = it.toString()
                        }, onDeletClick = {
                            ownerPhone = ownerPhone.minus(it)
                        })
                    }

                    GroupHeader(title = "Emails", onIconClick = {
                        selectedEditTypeList = RecordTypeList.OWNNER_EMAIL
                        selectedEditValue = null
                    })
                    ownerEmail.forEach {
                        ListItemComp(title = it, onIconClick = {
                            selectedEditTypeList = RecordTypeList.OWNNER_EMAIL
                            selectedEditValue = it
                        }, onDeletClick = {
                            ownerEmail = ownerEmail.minus(it)
                        })
                    }



                }
                if (recordType == RecordType.CONTACT) {
                    GroupHeader(title = "Phone numbers", onIconClick = {
                        selectedEditTypeList = RecordTypeList.PHONE
                        selectedEditValue = null
                    })
                    phoneNumbers.forEach {
                        ListItemComp(title = it.toString(), onIconClick = {
                            selectedEditTypeList = RecordTypeList.PHONE
                            selectedEditValue = it.toString()
                        }, onDeletClick = {
                            phoneNumbers = phoneNumbers.minus(it)
                        })
                    }

                    GroupHeader(title = "Emails", onIconClick = {
                        selectedEditTypeList = RecordTypeList.EMAIL
                        selectedEditValue = null
                    })
                    emails.forEach {
                        ListItemComp(title = it, onIconClick = {
                            selectedEditTypeList = RecordTypeList.EMAIL
                            selectedEditValue = it
                        }, onDeletClick = {
                            emails = emails.minus(it)
                        })
                    }
                    GroupHeader(title = "Websites", onIconClick = {
                        selectedEditTypeList = RecordTypeList.WEBSITE
                        selectedEditValue = null
                    })
                    websites.forEach {
                        ListItemComp(title = it, onIconClick = {
                            selectedEditTypeList = RecordTypeList.WEBSITE
                            selectedEditValue = it
                        }, onDeletClick = {
                            websites = websites.minus(it)
                        })
                    }
                    GroupHeader(title = "Social media", onIconClick = {
                        selectedEditTypeList = RecordTypeList.SOCIAL_MEDIA
                        selectedEditValue = null
                    })
                    socialMedias.forEach {
                        ListItemComp(title = it, onIconClick = {
                            selectedEditTypeList = RecordTypeList.SOCIAL_MEDIA
                            selectedEditValue = it
                        }, onDeletClick = {
                            socialMedias = socialMedias.minus(it)
                        })
                    }


                }


            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Button(
                    onClick = {
                        captureRecordUiModel.captureEventSink(
                            CaptureRecordEventSink.OnEditCaptureEvent(
                                billboardData = currentData?.copy(
                                    brandName = campaignBrand.text.toString().ifEmptySetNull()
                                        ?: currentData.brandName,
                                    brandCampaign = campaignDescription.text.toString()
                                        .ifEmptySetNull() ?: currentData.brandCampaign,
                                    billboardType = billboardType.ifEmptySetNull()
                                        ?: currentData.billboardType,
                                    billboardOwner = billboardOwner.text.toString().ifEmptySetNull()
                                        ?: currentData.billboardOwner,
                                    billboardWidth = billboardWidth.text.toString().ifEmptySetNull()
                                        ?: currentData.billboardWidth,
                                    billboardLength = billboardLength.text.toString()
                                        .ifEmptySetNull() ?: currentData.billboardLength,
                                    unitOfMeasurement = selectedUnitOfMeasurement.ifEmptySetNull()
                                        ?: currentData.unitOfMeasurement,
                                    phone = phoneNumbers,
                                    email = emails,
                                    siteUrl = websites,
                                    campainSocials = socialMedias,
                                    products = products,
                                    targetGender = targetGender.text.toString().ifEmptySetNull()
                                        ?: currentData.targetGender,
                                    targetAge = targetAge.text.toString().ifEmptySetNull()
                                        ?: currentData.targetAge,
                                    ownerWebsite = ownerWebsite.text.toString().ifEmptySetNull() ?: currentData.ownerWebsite,
                                    ownerContacts = ownerPhone,
                                    ownerEmail = ownerEmail,
                                    structure = selectedStructureType.ifEmptySetNull() ?: currentData.structure,
                                    material = selectedMaterialType.ifEmptySetNull() ?: currentData.material,
                                    angle = selectedAngleType.ifEmptySetNull() ?: currentData.angle,
                                    visibility = selectedVisibilityType.ifEmptySetNull() ?: currentData.visibility,
                                    illumination = selectedIlluminationType.ifEmptySetNull() ?: currentData.illumination,
                                    isOccupied = isOccupied ?: currentData.isOccupied,
                                ) ?: BillboardExtractedInfo(
                                    brandName = campaignBrand.text.toString().ifEmptySetNull()
                                        ?: currentData?.brandName,
                                    brandCampaign = campaignDescription.text.toString()
                                        .ifEmptySetNull() ?: currentData?.brandCampaign,
                                    billboardType = billboardType.ifEmptySetNull()
                                        ?: currentData?.billboardType,
                                    billboardOwner = billboardOwner.text.toString().ifEmptySetNull()
                                        ?: currentData?.billboardOwner,
                                    billboardWidth = billboardWidth.text.toString().ifEmptySetNull()
                                        ?: currentData?.billboardWidth,
                                    billboardLength = billboardLength.text.toString()
                                        .ifEmptySetNull() ?: currentData?.billboardLength,
                                    unitOfMeasurement = selectedUnitOfMeasurement.ifEmptySetNull()
                                        ?: currentData?.unitOfMeasurement,

                                    )
                            )
                        )
                        appState?.navController?.navigateUp()
                    },
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }


            }

        }

    }


}

@Composable
fun GroupHeader(
    title: String,
    icon: ImageVector? = Icons.Default.Add,
    onIconClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clickable(onClick = onIconClick ?: {})
            )
        }
    }
}


@Composable
fun ExtractedInfoEditText(
    textState: TextFieldState,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier
        .fillMaxWidth()
) {
    OutlinedTextField(
        modifier = modifier
            .height(50.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        contentPadding = PaddingValues(14.dp),
        state = textState,
        placeholder = { Text(placeholder) })
}

@Composable
fun ListItemComp(
    title: String,
    icon: ImageVector? = Icons.Outlined.Edit,
    onIconClick: (() -> Unit)? = null,
    onDeletClick: () -> Unit,
    modifier: Modifier = Modifier.padding(top = 6.dp)
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )

        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clickable(onClick = onIconClick ?: {})
            )
        }

        Icon(
            imageVector = Icons.Outlined.DeleteOutline,
            contentDescription = null,
            modifier = Modifier
                .padding(start = 8.dp)
                .clickable(onClick = onDeletClick)
        )


    }
}



