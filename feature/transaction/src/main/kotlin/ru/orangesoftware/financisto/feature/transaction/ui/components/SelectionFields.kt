package ru.orangesoftware.financisto.feature.transaction.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Category selection field
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectionField(
    selectedCategory: ru.orangesoftware.financisto.feature.transaction.CategoryOption?,
    availableCategories: List<ru.orangesoftware.financisto.feature.transaction.CategoryOption>,
    onCategorySelected: (ru.orangesoftware.financisto.feature.transaction.CategoryOption) -> Unit,
    onAddNewCategory: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedCategory?.title ?: "",
            onValueChange = { },
            readOnly = true,
            label = { Text("Category") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = null
                )
            }
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            for (category in availableCategories) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = category.title,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )
            }
            
            // Add New Category option
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Add New Category",
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                onClick = {
                    onAddNewCategory()
                    expanded = false
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )
        }
    }
}

/**
 * Payee selection field
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayeeSelectionField(
    selectedPayee: ru.orangesoftware.financisto.feature.transaction.PayeeOption?,
    availablePayees: List<ru.orangesoftware.financisto.feature.transaction.PayeeOption>,
    onPayeeSelected: (ru.orangesoftware.financisto.feature.transaction.PayeeOption) -> Unit,
    onAddNewPayee: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedPayee?.name ?: "",
            onValueChange = { },
            readOnly = true,
            label = { Text("Payee") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null
                )
            }
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            for (payee in availablePayees) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = payee.name,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    onClick = {
                        onPayeeSelected(payee)
                        expanded = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )
            }
            
            // Add New Payee option
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Add New Payee",
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                onClick = {
                    onAddNewPayee()
                    expanded = false
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )
        }
    }
}

/**
 * Project selection field
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectSelectionField(
    selectedProject: ru.orangesoftware.financisto.feature.transaction.ProjectOption?,
    availableProjects: List<ru.orangesoftware.financisto.feature.transaction.ProjectOption>,
    onProjectSelected: (ru.orangesoftware.financisto.feature.transaction.ProjectOption) -> Unit,
    onAddNewProject: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedProject?.name ?: "",
            onValueChange = { },
            readOnly = true,
            label = { Text("Project") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null
                )
            }
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            for (project in availableProjects) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = project.name,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    onClick = {
                        onProjectSelected(project)
                        expanded = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )
            }
            
            // Add New Project option
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Add New Project",
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                onClick = {
                    onAddNewProject()
                    expanded = false
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )
        }
    }
}

